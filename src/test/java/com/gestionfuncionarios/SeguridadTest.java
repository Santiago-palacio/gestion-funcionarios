package com.gestionfuncionarios;

import com.gestionfuncionarios.dao.UsuarioDAO;
import com.gestionfuncionarios.modelo.Rol;
import com.gestionfuncionarios.modelo.Usuario;
import com.gestionfuncionarios.seguridad.AccesoDenegadoException;
import com.gestionfuncionarios.seguridad.ControlAcceso;
import com.gestionfuncionarios.seguridad.PasswordHasher;
import com.gestionfuncionarios.seguridad.ServicioAutenticacion;
import com.gestionfuncionarios.seguridad.ServicioAutenticacion.ResultadoAutenticacion;
import com.gestionfuncionarios.seguridad.ServicioUsuario;
import com.gestionfuncionarios.seguridad.SesionUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de Seguridad, Autenticacion y Autorizacion por Roles (JUnit 5).
 * Valida las reglas de negocio, hashing seguro y control de acceso en memoria
 * sin requerir conexion activa a MySQL.
 */
public class SeguridadTest {

    private UsuarioDAO usuarioDAOMock;
    private ServicioAutenticacion servicioAuth;
    private ServicioUsuario servicioUsuario;

    // Hash BCrypt precalculado para "Admin123*"
    private String hashAdminValido;
    // Hash BCrypt precalculado para "Consulta123*"
    private String hashConsultaValido;

    @BeforeEach
    public void setUp() {
        // Asegurar que no quede sesion remanente entre pruebas
        SesionUsuario.getInstancia().cerrarSesion();

        hashAdminValido = PasswordHasher.hashear("Admin123*");
        hashConsultaValido = PasswordHasher.hashear("Consulta123*");

        // Implementacion DAO en memoria para pruebas unitarias sin base de datos
        usuarioDAOMock = new UsuarioDAO() {
            private final Map<String, Usuario> usuarios = new HashMap<>();
            private int consecutivo = 1;

            {
                Usuario admin = new Usuario(consecutivo++, "admin", hashAdminValido, Rol.ADMINISTRADOR, true, null);
                Usuario consulta = new Usuario(consecutivo++, "consulta", hashConsultaValido, Rol.CONSULTA, true, null);
                Usuario inactivo = new Usuario(consecutivo++, "inactivo", hashAdminValido, Rol.ADMINISTRADOR, false, null);

                usuarios.put(admin.getUsername().toLowerCase(), admin);
                usuarios.put(consulta.getUsername().toLowerCase(), consulta);
                usuarios.put(inactivo.getUsername().toLowerCase(), inactivo);
            }

            @Override
            public Usuario buscarPorUsername(String username) throws SQLException {
                if (username == null) return null;
                return usuarios.get(username.trim().toLowerCase());
            }

            @Override
            public Usuario buscarPorId(int idUsuario) throws SQLException {
                return usuarios.values().stream()
                        .filter(u -> u.getIdUsuario() != null && u.getIdUsuario() == idUsuario)
                        .findFirst().orElse(null);
            }

            @Override
            public List<Usuario> listarTodos() throws SQLException {
                return new ArrayList<>(usuarios.values());
            }

            @Override
            public boolean registrar(Usuario usuario) throws SQLException {
                if (usuarios.containsKey(usuario.getUsername().toLowerCase())) {
                    throw new SQLException("El nombre de usuario ya existe.", "23000", 1062);
                }
                usuario.setIdUsuario(consecutivo++);
                usuarios.put(usuario.getUsername().toLowerCase(), usuario);
                return true;
            }

            @Override
            public boolean actualizarEstado(int idUsuario, boolean activo) throws SQLException {
                Usuario u = buscarPorId(idUsuario);
                if (u != null) {
                    u.setActivo(activo);
                    return true;
                }
                return false;
            }

            @Override
            public boolean cambiarPassword(int idUsuario, String nuevoPasswordHash) throws SQLException {
                Usuario u = buscarPorId(idUsuario);
                if (u != null) {
                    u.setPasswordHash(nuevoPasswordHash);
                    return true;
                }
                return false;
            }

            @Override
            public boolean existeUsername(String username) throws SQLException {
                return username != null && usuarios.containsKey(username.trim().toLowerCase());
            }

            @Override
            public boolean actualizarRol(int idUsuario, int idRol) throws SQLException {
                Usuario u = buscarPorId(idUsuario);
                if (u != null) {
                    u.setRol(Rol.fromId(idRol));
                    return true;
                }
                return false;
            }

            @Override
            public boolean actualizarUsuario(int idUsuario, String username, int idRol, boolean activo) throws SQLException {
                Usuario u = buscarPorId(idUsuario);
                if (u != null) {
                    u.setUsername(username);
                    u.setRol(Rol.fromId(idRol));
                    u.setActivo(activo);
                    return true;
                }
                return false;
            }

            @Override
            public int contarAdministradoresActivos() throws SQLException {
                return (int) usuarios.values().stream()
                        .filter(u -> u.isActivo() && u.getRol() == Rol.ADMINISTRADOR)
                        .count();
            }
        };

        servicioAuth = new ServicioAutenticacion(usuarioDAOMock);
        servicioUsuario = new ServicioUsuario(usuarioDAOMock);
    }

    @AfterEach
    public void tearDown() {
        SesionUsuario.getInstancia().cerrarSesion();
    }

    // ==============================================================
    // 1. PRUEBAS DE HASHING CRIPTOGRAFICO (PasswordHasher & BCrypt)
    // ==============================================================

    @Test
    @DisplayName("PasswordHasher debe generar hashes con sal unica para la misma contraseña")
    public void testPasswordHasherSalUnica() {
        String pass = "MiClaveSegura2024*";
        String hash1 = PasswordHasher.hashear(pass);
        String hash2 = PasswordHasher.hashear(pass);

        assertNotNull(hash1);
        assertNotNull(hash2);
        // Regla fundamental de BCrypt: dos hashes de la misma clave DEBEN ser distintos por la sal
        assertNotEquals(hash1, hash2);

        // Sin embargo, ambos deben verificar con exito la misma contraseña
        assertTrue(PasswordHasher.verificar(pass, hash1));
        assertTrue(PasswordHasher.verificar(pass, hash2));
    }

    @Test
    @DisplayName("PasswordHasher debe rechazar contraseñas incorrectas o hashes invalidos")
    public void testPasswordHasherRechazo() {
        String hash = PasswordHasher.hashear("ClaveOriginal");

        assertFalse(PasswordHasher.verificar("ClaveEquivocada", hash));
        assertFalse(PasswordHasher.verificar("", hash));
        assertFalse(PasswordHasher.verificar((String) null, hash));
        assertFalse(PasswordHasher.verificar("ClaveOriginal", "hash_invalido_de_prueba"));
        assertFalse(PasswordHasher.verificar("ClaveOriginal", null));
    }

    // ==============================================================
    // 2. PRUEBAS DE AUTENTICACION (ServicioAutenticacion)
    // ==============================================================

    @Test
    @DisplayName("Autenticacion exitosa con credenciales correctas para Administrador")
    public void testAutenticacionAdminExitosa() {
        ResultadoAutenticacion res = servicioAuth.autenticar("admin", "Admin123*");

        assertTrue(res.isExitoso());
        assertNotNull(res.getUsuario());
        assertEquals("admin", res.getUsuario().getUsername());
        assertEquals(Rol.ADMINISTRADOR, res.getUsuario().getRol());

        // Comprueba que la sesion centralizada se haya establecido
        assertTrue(SesionUsuario.getInstancia().haySesionActiva());
        assertTrue(SesionUsuario.getInstancia().esAdministrador());
        assertEquals("admin", SesionUsuario.getInstancia().getUsernameActual());
    }

    @Test
    @DisplayName("Autenticacion exitosa con credenciales correctas para rol Consulta")
    public void testAutenticacionConsultaExitosa() {
        ResultadoAutenticacion res = servicioAuth.autenticar("consulta", "Consulta123*");

        assertTrue(res.isExitoso());
        assertNotNull(res.getUsuario());
        assertEquals("consulta", res.getUsuario().getUsername());
        assertEquals(Rol.CONSULTA, res.getUsuario().getRol());

        assertTrue(SesionUsuario.getInstancia().haySesionActiva());
        assertTrue(SesionUsuario.getInstancia().esConsulta());
        assertFalse(SesionUsuario.getInstancia().esAdministrador());
    }

    @Test
    @DisplayName("Autenticacion fallida por contraseña incorrecta debe mostrar mensaje neutral")
    public void testAutenticacionContrasenaIncorrecta() {
        ResultadoAutenticacion res = servicioAuth.autenticar("admin", "ClaveErronea999");

        assertFalse(res.isExitoso());
        assertNull(res.getUsuario());
        // Mensaje neutral para mitigar enumeracion de cuentas
        assertEquals("Usuario o contraseña incorrectos.", res.getMensaje());
        assertFalse(SesionUsuario.getInstancia().haySesionActiva());
    }

    @Test
    @DisplayName("Autenticacion fallida por usuario inexistente debe mostrar mensaje neutral")
    public void testAutenticacionUsuarioInexistente() {
        ResultadoAutenticacion res = servicioAuth.autenticar("usuario_inexistente", "CualquierClave");

        assertFalse(res.isExitoso());
        assertNull(res.getUsuario());
        assertEquals("Usuario o contraseña incorrectos.", res.getMensaje());
        assertFalse(SesionUsuario.getInstancia().haySesionActiva());
    }

    @Test
    @DisplayName("Autenticacion rechazada para usuario con cuenta inactiva")
    public void testAutenticacionUsuarioInactivo() {
        ResultadoAutenticacion res = servicioAuth.autenticar("inactivo", "Admin123*");

        assertFalse(res.isExitoso());
        assertNull(res.getUsuario());
        assertTrue(res.getMensaje().contains("inactiva"));
        assertFalse(SesionUsuario.getInstancia().haySesionActiva());
    }

    @Test
    @DisplayName("Autenticacion debe rechazar campos vacios o nulos")
    public void testAutenticacionCamposVacios() {
        ResultadoAutenticacion r1 = servicioAuth.autenticar("", "Admin123*");
        assertFalse(r1.isExitoso());

        ResultadoAutenticacion r2 = servicioAuth.autenticar("admin", "");
        assertFalse(r2.isExitoso());

        ResultadoAutenticacion r3 = servicioAuth.autenticar(null, (char[]) null);
        assertFalse(r3.isExitoso());
    }

    // ==============================================================
    // 3. PRUEBAS DE CIERRE DE SESION (SesionUsuario)
    // ==============================================================

    @Test
    @DisplayName("El cierre de sesion debe limpiar completamente el estado del usuario")
    public void testCierreDeSesion() {
        servicioAuth.autenticar("admin", "Admin123*");
        assertTrue(SesionUsuario.getInstancia().haySesionActiva());

        // Cierre de sesion
        servicioAuth.cerrarSesion();

        assertFalse(SesionUsuario.getInstancia().haySesionActiva());
        assertNull(SesionUsuario.getInstancia().getUsuarioActual());
        assertNull(SesionUsuario.getInstancia().getRolActual());
        assertEquals("Invitado", SesionUsuario.getInstancia().getUsernameActual());
    }

    // ==============================================================
    // 4. PRUEBAS DE AUTORIZACION Y CONTROL DE PERMISOS (ControlAcceso)
    // ==============================================================

    @Test
    @DisplayName("Administrador debe tener permiso para todas las operaciones de escritura")
    public void testPermisosAdministrador() {
        servicioAuth.autenticar("admin", "Admin123*");

        assertTrue(ControlAcceso.tienePermisoEscritura());
        assertTrue(ControlAcceso.tienePermisoLectura());

        // No debe lanzar excepciones
        assertDoesNotThrow(() -> ControlAcceso.validarPermisoEscritura("Registrar Funcionario"));
        assertDoesNotThrow(() -> ControlAcceso.validarPermisoEscritura("Actualizar Funcionario"));
        assertDoesNotThrow(() -> ControlAcceso.validarPermisoEscritura("Eliminar Funcionario"));
        assertDoesNotThrow(() -> ControlAcceso.validarPermisoEscritura("Gestionar Grupo Familiar"));
        assertDoesNotThrow(() -> ControlAcceso.validarPermisoEscritura("Gestionar Estudios"));
    }

    @Test
    @DisplayName("Rol Consulta debe tener lectura permitida pero escritura bloqueada con AccesoDenegadoException")
    public void testBloqueoEscrituraRolConsulta() {
        servicioAuth.autenticar("consulta", "Consulta123*");

        assertFalse(ControlAcceso.tienePermisoEscritura());
        assertTrue(ControlAcceso.tienePermisoLectura());

        // Bloqueo de Registrar
        AccesoDenegadoException exRegistro = assertThrows(
                AccesoDenegadoException.class,
                () -> ControlAcceso.validarPermisoEscritura("Registrar Funcionario")
        );
        assertTrue(exRegistro.getMessage().contains("CONSULTA"));

        // Bloqueo de Actualizar
        AccesoDenegadoException exActualizar = assertThrows(
                AccesoDenegadoException.class,
                () -> ControlAcceso.validarPermisoEscritura("Actualizar Funcionario")
        );
        assertTrue(exActualizar.getMessage().contains("CONSULTA"));

        // Bloqueo de Eliminar
        AccesoDenegadoException exEliminar = assertThrows(
                AccesoDenegadoException.class,
                () -> ControlAcceso.validarPermisoEscritura("Eliminar Funcionario")
        );
        assertTrue(exEliminar.getMessage().contains("CONSULTA"));

        // Bloqueo de Familiares y Estudios
        assertThrows(
                AccesoDenegadoException.class,
                () -> ControlAcceso.validarPermisoEscritura("Agregar Familiar")
        );
        assertThrows(
                AccesoDenegadoException.class,
                () -> ControlAcceso.validarPermisoEscritura("Eliminar Estudio")
        );
    }

    @Test
    @DisplayName("Operaciones protegidas sin sesion iniciada deben lanzar AccesoDenegadoException")
    public void testOperacionesSinSesion() {
        SesionUsuario.getInstancia().cerrarSesion();

        assertFalse(ControlAcceso.tienePermisoEscritura());
        assertFalse(ControlAcceso.tienePermisoLectura());

        assertThrows(AccesoDenegadoException.class, () -> ControlAcceso.validarAutenticacion());
        assertThrows(AccesoDenegadoException.class, () -> ControlAcceso.validarPermisoEscritura("Cualquier Operacion"));
    }

    @Test
    @DisplayName("El modelo Usuario debe enmascarar la contraseña en su metodo toString()")
    public void testProteccionContrasenaEnToString() {
        Usuario u = new Usuario("usuarioPrueba", "$2a$12$hashSensibleDePruebaQueNoDebeVerse", Rol.ADMINISTRADOR, true);
        String representacion = u.toString();

        assertNotNull(representacion);
        assertTrue(representacion.contains("[PROTEGIDO]"));
        assertFalse(representacion.contains("hashSensibleDePruebaQueNoDebeVerse"));
    }

    // ==============================================================
    // 5. PRUEBAS DEL MODULO DE GESTION DE USUARIOS (ServicioUsuario)
    // ==============================================================

    @Test
    @DisplayName("ADMINISTRADOR puede crear un nuevo usuario con contraseña cifrada en BCrypt")
    public void testGestionUsuariosCrearUsuarioPorAdmin() throws SQLException {
        servicioAuth.autenticar("admin", "Admin123*");

        Usuario creado = servicioUsuario.crearUsuario("nuevo_operador", "ClaveSegura123*", Rol.CONSULTA);

        assertNotNull(creado);
        assertEquals("nuevo_operador", creado.getUsername());
        assertEquals(Rol.CONSULTA, creado.getRol());
        assertTrue(creado.isActivo());
        assertTrue(PasswordHasher.verificar("ClaveSegura123*", creado.getPasswordHash()));

        // Verificar que aparezca en el listado
        List<Usuario> lista = servicioUsuario.listarUsuarios();
        assertTrue(lista.stream().anyMatch(u -> u.getUsername().equals("nuevo_operador")));
    }

    @Test
    @DisplayName("Gestion de usuarios debe rechazar nombres duplicados o campos invalidos")
    public void testGestionUsuariosRechazoDuplicadosYCamposInvalidos() {
        servicioAuth.autenticar("admin", "Admin123*");

        // Nombre duplicado
        assertThrows(IllegalArgumentException.class, () ->
                servicioUsuario.crearUsuario("admin", "OtraClave123*", Rol.CONSULTA)
        );

        // Nombre vacio
        assertThrows(IllegalArgumentException.class, () ->
                servicioUsuario.crearUsuario("", "Clave123*", Rol.CONSULTA)
        );

        // Contraseña corta (< 6 caracteres)
        assertThrows(IllegalArgumentException.class, () ->
                servicioUsuario.crearUsuario("usuario_test", "123", Rol.CONSULTA)
        );

        // Rol nulo
        assertThrows(IllegalArgumentException.class, () ->
                servicioUsuario.crearUsuario("usuario_test", "ClaveValida123*", null)
        );
    }

    @Test
    @DisplayName("Rol CONSULTA tiene terminantemente bloqueadas todas las operaciones de Gestion de Usuarios")
    public void testGestionUsuariosBloqueoRolConsulta() {
        servicioAuth.autenticar("consulta", "Consulta123*");

        assertThrows(AccesoDenegadoException.class, () -> servicioUsuario.listarUsuarios());
        assertThrows(AccesoDenegadoException.class, () ->
                servicioUsuario.crearUsuario("hacker", "Clave123*", Rol.ADMINISTRADOR)
        );
        assertThrows(AccesoDenegadoException.class, () ->
                servicioUsuario.cambiarRol(2, Rol.ADMINISTRADOR)
        );
        assertThrows(AccesoDenegadoException.class, () ->
                servicioUsuario.cambiarEstado(1, false)
        );
        assertThrows(AccesoDenegadoException.class, () ->
                servicioUsuario.restablecerPassword(1, "NuevaClave123*")
        );
        assertThrows(AccesoDenegadoException.class, () ->
                servicioUsuario.actualizarUsuario(2, "consulta_editado", Rol.ADMINISTRADOR, true)
        );
    }

    @Test
    @DisplayName("Proteccion de seguridad: No se permite desactivar ni cambiar de rol al ultimo administrador activo")
    public void testGestionUsuariosProteccionUltimoAdministrador() throws SQLException {
        servicioAuth.autenticar("admin", "Admin123*");

        // Inicialmente solo hay 1 administrador activo (admin).
        // 1. Intentar desactivar al unico admin activo debe fallar
        assertThrows(AccesoDenegadoException.class, () ->
                servicioUsuario.cambiarEstado(1, false)
        );

        // 2. Intentar cambiar su rol a CONSULTA debe fallar
        assertThrows(AccesoDenegadoException.class, () ->
                servicioUsuario.cambiarRol(1, Rol.CONSULTA)
        );

        // 3. Si se crea un SEGUNDO administrador activo...
        Usuario segundoAdmin = servicioUsuario.crearUsuario("admin2", "Admin2Pass123*", Rol.ADMINISTRADOR);
        assertNotNull(segundoAdmin);

        // Ahora si debe permitir cambiar de rol o desactivar a uno de ellos
        assertDoesNotThrow(() -> servicioUsuario.cambiarRol(segundoAdmin.getIdUsuario(), Rol.CONSULTA));
    }

    @Test
    @DisplayName("ADMINISTRADOR puede activar y desactivar usuarios normales")
    public void testGestionUsuariosActivarDesactivar() throws SQLException {
        servicioAuth.autenticar("admin", "Admin123*");

        // Usuario consulta (ID: 2) esta activo inicialmente
        Usuario consulta = usuarioDAOMock.buscarPorId(2);
        assertTrue(consulta.isActivo());

        // Desactivar
        servicioUsuario.cambiarEstado(2, false);
        assertFalse(usuarioDAOMock.buscarPorId(2).isActivo());

        // Reactivar
        servicioUsuario.cambiarEstado(2, true);
        assertTrue(usuarioDAOMock.buscarPorId(2).isActivo());
    }

    @Test
    @DisplayName("ADMINISTRADOR puede restablecer la contraseña con BCrypt")
    public void testGestionUsuariosRestablecerPassword() throws SQLException {
        servicioAuth.autenticar("admin", "Admin123*");

        String nuevaClave = "ClaveRestablecida2024*";
        servicioUsuario.restablecerPassword(2, nuevaClave);

        Usuario consultaActualizado = usuarioDAOMock.buscarPorId(2);
        assertTrue(PasswordHasher.verificar(nuevaClave, consultaActualizado.getPasswordHash()));

        // Intentar restablecer con clave vacia o muy corta debe fallar
        assertThrows(IllegalArgumentException.class, () ->
                servicioUsuario.restablecerPassword(2, "123")
        );
    }
}
