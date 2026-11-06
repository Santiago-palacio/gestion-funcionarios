package com.gestionfuncionarios.seguridad;

import com.gestionfuncionarios.dao.UsuarioDAO;
import com.gestionfuncionarios.dao.UsuarioDAOImpl;
import com.gestionfuncionarios.modelo.Rol;
import com.gestionfuncionarios.modelo.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio de logica de negocio para el modulo de Gestion de Usuarios.
 * 
 * Principios y reglas de seguridad aplicadas:
 * - Toda operacion exige autorizacion mediante ControlAcceso (solo ADMINISTRADOR).
 * - Proteccion contra desabastecimiento: Prohibido desactivar o degradar al ultimo
 *   administrador activo del sistema para evitar bloqueos accidentales.
 * - Hashing seguro de contraseñas mediante BCrypt (coste 12 y sal automatica).
 * - Validacion de duplicados y campos vacios.
 */
public class ServicioUsuario {

    private final UsuarioDAO usuarioDAO;

    public ServicioUsuario() {
        this(new UsuarioDAOImpl());
    }

    public ServicioUsuario(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Retorna la lista completa de usuarios registrados.
     * Requiere permisos de ADMINISTRADOR.
     */
    public List<Usuario> listarUsuarios() throws SQLException {
        ControlAcceso.validarPermisoEscritura("Consultar listado de usuarios");
        return usuarioDAO.listarTodos();
    }

    /**
     * Crea un nuevo usuario en el sistema con contraseña cifrada mediante BCrypt.
     */
    public Usuario crearUsuario(String username, String password, Rol rol) throws SQLException {
        ControlAcceso.validarPermisoEscritura("Crear usuario");

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        String userLimpio = username.trim();
        if (userLimpio.length() < 3 || userLimpio.length() > 50) {
            throw new IllegalArgumentException("El nombre de usuario debe tener entre 3 y 50 caracteres.");
        }
        if (!userLimpio.matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException("El nombre de usuario solo puede contener letras, numeros, puntos, guiones o guion bajo.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña inicial es obligatoria.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        if (rol == null) {
            throw new IllegalArgumentException("Debe seleccionar un rol para el usuario.");
        }

        if (usuarioDAO.existeUsername(userLimpio)) {
            throw new IllegalArgumentException("El nombre de usuario '" + userLimpio + "' ya se encuentra registrado.");
        }

        String passwordHash = PasswordHasher.hashear(password);
        Usuario nuevo = new Usuario(userLimpio, passwordHash, rol, true);

        boolean registrado = usuarioDAO.registrar(nuevo);
        if (!registrado) {
            throw new SQLException("No se pudo registrar el nuevo usuario en la base de datos.");
        }

        return nuevo;
    }

    /**
     * Cambia el rol de un usuario existente.
     * Si el usuario es ADMINISTRADOR y se desea cambiar a CONSULTA,
     * comprueba que no sea el unico administrador activo.
     */
    public void cambiarRol(int idUsuario, Rol nuevoRol) throws SQLException {
        ControlAcceso.validarPermisoEscritura("Cambiar rol de usuario");

        if (nuevoRol == null) {
            throw new IllegalArgumentException("El nuevo rol no puede ser nulo.");
        }

        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontro el usuario especificado.");
        }

        if (usuario.getRol() == Rol.ADMINISTRADOR && nuevoRol != Rol.ADMINISTRADOR) {
            int adminsActivos = usuarioDAO.contarAdministradoresActivos();
            if (adminsActivos <= 1 && usuario.isActivo()) {
                throw new AccesoDenegadoException(
                        "Operacion rechazada por seguridad: No es posible cambiar el rol del unico administrador activo del sistema."
                );
            }
        }

        usuarioDAO.actualizarRol(idUsuario, nuevoRol.getIdRol());
    }

    /**
     * Activa o desactiva la cuenta de un usuario.
     * Si se intenta desactivar a un ADMINISTRADOR, se verifica que existan otros administradores activos.
     */
    public void cambiarEstado(int idUsuario, boolean nuevoEstado) throws SQLException {
        ControlAcceso.validarPermisoEscritura("Activar o desactivar usuario");

        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontro el usuario especificado.");
        }

        // Si se intenta desactivar (nuevoEstado == false) y es Administrador activo
        if (!nuevoEstado && usuario.isActivo() && usuario.getRol() == Rol.ADMINISTRADOR) {
            int adminsActivos = usuarioDAO.contarAdministradoresActivos();
            if (adminsActivos <= 1) {
                throw new AccesoDenegadoException(
                        "Operacion rechazada por seguridad: No es posible desactivar al unico administrador activo del sistema."
                );
            }
        }

        usuarioDAO.actualizarEstado(idUsuario, nuevoEstado);
    }

    /**
     * Restablece la contraseña de un usuario generando un nuevo hash seguro con BCrypt.
     */
    public void restablecerPassword(int idUsuario, String nuevaPassword) throws SQLException {
        ControlAcceso.validarPermisoEscritura("Restablecer contraseña de usuario");

        if (nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacia.");
        }
        if (nuevaPassword.length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontro el usuario especificado.");
        }

        String nuevoHash = PasswordHasher.hashear(nuevaPassword);
        usuarioDAO.cambiarPassword(idUsuario, nuevoHash);
    }

    /**
     * Actualiza la informacion completa de un usuario (username, rol y estado).
     */
    public void actualizarUsuario(int idUsuario, String nuevoUsername, Rol nuevoRol, boolean nuevoEstado) throws SQLException {
        ControlAcceso.validarPermisoEscritura("Actualizar datos de usuario");

        if (nuevoUsername == null || nuevoUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        String userLimpio = nuevoUsername.trim();
        if (userLimpio.length() < 3 || userLimpio.length() > 50) {
            throw new IllegalArgumentException("El nombre de usuario debe tener entre 3 y 50 caracteres.");
        }
        if (nuevoRol == null) {
            throw new IllegalArgumentException("Debe seleccionar un rol valido.");
        }

        Usuario actual = usuarioDAO.buscarPorId(idUsuario);
        if (actual == null) {
            throw new IllegalArgumentException("No se encontro el usuario especificado.");
        }

        // Validacion de seguridad: evitar deshabilitar o quitar permisos al ultimo admin activo
        if (actual.getRol() == Rol.ADMINISTRADOR && actual.isActivo()) {
            boolean pierdeRolAdmin = (nuevoRol != Rol.ADMINISTRADOR);
            boolean seDesactiva = !nuevoEstado;

            if (pierdeRolAdmin || seDesactiva) {
                int adminsActivos = usuarioDAO.contarAdministradoresActivos();
                if (adminsActivos <= 1) {
                    throw new AccesoDenegadoException(
                            "Operacion rechazada por seguridad: Esta cuenta es el unico administrador activo del sistema.\n" +
                            "No se le puede retirar el rol ni desactivar sin antes habilitar a otro administrador."
                    );
                }
            }
        }

        usuarioDAO.actualizarUsuario(idUsuario, userLimpio, nuevoRol.getIdRol(), nuevoEstado);
    }
}

