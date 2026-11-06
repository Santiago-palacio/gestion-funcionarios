package com.gestionfuncionarios.dao;

import com.gestionfuncionarios.modelo.Usuario;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato DAO para las operaciones de persistencia de usuarios del sistema.
 */
public interface UsuarioDAO {

    /**
     * Busca un usuario por su nombre de usuario (username).
     * 
     * @param username Nombre de usuario a buscar
     * @return Objeto Usuario encontrado o null si no existe
     * @throws SQLException Si ocurre un fallo en la conexion o consulta
     */
    Usuario buscarPorUsername(String username) throws SQLException;

    /**
     * Busca un usuario por su identificador primario.
     * 
     * @param idUsuario Identificador de usuario
     * @return Objeto Usuario encontrado o null si no existe
     * @throws SQLException Si ocurre un error en la base de datos
     */
    Usuario buscarPorId(int idUsuario) throws SQLException;

    /**
     * Retorna el listado completo de usuarios registrados.
     * 
     * @return Lista de usuarios
     * @throws SQLException Si ocurre un error en la consulta
     */
    List<Usuario> listarTodos() throws SQLException;

    /**
     * Registra un nuevo usuario en la base de datos con su contraseña hasheada.
     * 
     * @param usuario Usuario a registrar
     * @return true si el registro fue exitoso
     * @throws SQLException Si ocurre error de duplicidad o base de datos
     */
    boolean registrar(Usuario usuario) throws SQLException;

    /**
     * Actualiza el estado activo/inactivo de un usuario.
     * 
     * @param idUsuario ID del usuario
     * @param activo Nuevo estado
     * @return true si se actualizo correctamente
     * @throws SQLException Si ocurre un error
     */
    boolean actualizarEstado(int idUsuario, boolean activo) throws SQLException;

    /**
     * Actualiza el hash de contraseña de un usuario.
     * 
     * @param idUsuario ID del usuario
     * @param nuevoPasswordHash Nuevo hash seguro de contraseña
     * @return true si se actualizo correctamente
     * @throws SQLException Si ocurre un error
     */
    boolean cambiarPassword(int idUsuario, String nuevoPasswordHash) throws SQLException;

    /**
     * Verifica si un username ya esta registrado.
     * 
     * @param username Nombre de usuario
     * @return true si ya existe
     * @throws SQLException Si ocurre un error
     */
    boolean existeUsername(String username) throws SQLException;

    /**
     * Actualiza el rol asignado a un usuario.
     * 
     * @param idUsuario ID del usuario
     * @param idRol ID del nuevo rol
     * @return true si se actualizo correctamente
     * @throws SQLException Si ocurre un error
     */
    boolean actualizarRol(int idUsuario, int idRol) throws SQLException;

    /**
     * Actualiza los datos basicos de un usuario (username, rol y estado activo).
     * 
     * @param idUsuario ID del usuario
     * @param username Nuevo nombre de usuario
     * @param idRol ID del rol
     * @param activo Estado de la cuenta
     * @return true si se actualizo correctamente
     * @throws SQLException Si ocurre un error o nombre duplicado
     */
    boolean actualizarUsuario(int idUsuario, String username, int idRol, boolean activo) throws SQLException;

    /**
     * Cuenta cuantos administradores activos existen actualmente en el sistema.
     * Util para prevenir el bloqueo accidental del ultimo administrador.
     * 
     * @return Cantidad de administradores activos
     * @throws SQLException Si ocurre un error
     */
    int contarAdministradoresActivos() throws SQLException;
}

