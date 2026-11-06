package com.gestionfuncionarios.seguridad;

import com.gestionfuncionarios.dao.UsuarioDAO;
import com.gestionfuncionarios.dao.UsuarioDAOImpl;
import com.gestionfuncionarios.modelo.Usuario;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * Servicio de logica de negocio para el proceso de autenticacion de usuarios.
 * 
 * Reglas de seguridad:
 * - Mensajes neutrales en caso de fallo ("Usuario o contraseña incorrectos")
 *   para evitar ataques de enumeracion de nombres de usuario.
 * - Rechazo estricto de cuentas inactivas.
 * - Limpieza obligatoria de arreglos de contraseñas de memoria.
 * - Establece la sesion en SesionUsuario tras un login exitoso.
 */
public class ServicioAutenticacion {

    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos.";
    private static final String MENSAJE_USUARIO_INACTIVO = "La cuenta de usuario se encuentra inactiva. Contacte al administrador.";

    private final UsuarioDAO usuarioDAO;

    public ServicioAutenticacion() {
        this(new UsuarioDAOImpl());
    }

    public ServicioAutenticacion(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Resultado tipado que describe el desenlace de un intento de autenticacion.
     */
    public static class ResultadoAutenticacion {
        private final boolean exitoso;
        private final String mensaje;
        private final Usuario usuario;

        private ResultadoAutenticacion(boolean exitoso, String mensaje, Usuario usuario) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
            this.usuario = usuario;
        }

        public static ResultadoAutenticacion exito(Usuario usuario) {
            return new ResultadoAutenticacion(true, "Autenticacion exitosa.", usuario);
        }

        public static ResultadoAutenticacion fallo(String mensaje) {
            return new ResultadoAutenticacion(false, mensaje, null);
        }

        public boolean isExitoso() {
            return exitoso;
        }

        public String getMensaje() {
            return mensaje;
        }

        public Usuario getUsuario() {
            return usuario;
        }
    }

    /**
     * Autentica un usuario verificando credenciales seguras mediante BCrypt.
     * 
     * @param username Nombre de usuario ingresado
     * @param password Caracteres de la contraseña (char[])
     * @return ResultadoAutenticacion con el estatus y mensaje correspondiente
     */
    public ResultadoAutenticacion autenticar(String username, char[] password) {
        if (username == null || username.trim().isEmpty() || password == null || password.length == 0) {
            return ResultadoAutenticacion.fallo("Por favor complete tanto el usuario como la contraseña.");
        }

        try {
            Usuario usuario = usuarioDAO.buscarPorUsername(username.trim());

            // 1. Validar existencia del usuario
            if (usuario == null) {
                // Mensaje neutral: no revelar si el usuario existe o no
                return ResultadoAutenticacion.fallo(MENSAJE_CREDENCIALES_INVALIDAS);
            }

            // 2. Validar estado de la cuenta (activo/inactivo)
            if (!usuario.isActivo()) {
                return ResultadoAutenticacion.fallo(MENSAJE_USUARIO_INACTIVO);
            }

            // 3. Verificar hash criptografico con BCrypt
            boolean passwordValida = PasswordHasher.verificar(password, usuario.getPasswordHash());
            if (!passwordValida) {
                return ResultadoAutenticacion.fallo(MENSAJE_CREDENCIALES_INVALIDAS);
            }

            // 4. Iniciar sesion centralizada en memoria
            SesionUsuario.getInstancia().iniciarSesion(usuario);

            return ResultadoAutenticacion.exito(usuario);

        } catch (SQLException e) {
            return ResultadoAutenticacion.fallo("Error de comunicacion con la base de datos: " + e.getMessage());
        } finally {
            // Limpieza proactiva de la contraseña en memoria
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Sobrecarga de conveniencia que acepta contraseñas como String.
     */
    public ResultadoAutenticacion autenticar(String username, String password) {
        if (password == null) {
            return autenticar(username, new char[0]);
        }
        return autenticar(username, password.toCharArray());
    }

    /**
     * Cierra la sesion del usuario activo.
     */
    public void cerrarSesion() {
        SesionUsuario.getInstancia().cerrarSesion();
    }
}

