package com.gestionfuncionarios.dao;

import com.gestionfuncionarios.modelo.Funcionario;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz DAO que define las operaciones de persistencia para Funcionario.
 */
public interface FuncionarioDAO {

    /**
     * Registra un nuevo funcionario en la base de datos.
     * @param funcionario Objeto con los datos a registrar.
     * @return true si se registro correctamente.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    boolean registrar(Funcionario funcionario) throws SQLException;

    /**
     * Retorna todos los funcionarios registrados.
     * @return Lista de funcionarios.
     * @throws SQLException Si ocurre un error en la consulta.
     */
    List<Funcionario> listarTodos() throws SQLException;

    /**
     * Busca un funcionario por su identificador primario.
     * @param idFuncionario ID del funcionario.
     * @return Funcionario encontrado o null si no existe.
     * @throws SQLException Si ocurre un error en la consulta.
     */
    Funcionario buscarPorId(int idFuncionario) throws SQLException;

    /**
     * Busca un funcionario por su numero de documento de identidad.
     * @param numeroDocumento Numero de identificacion.
     * @return Funcionario encontrado o null si no existe.
     * @throws SQLException Si ocurre un error en la consulta.
     */
    Funcionario buscarPorDocumento(String numeroDocumento) throws SQLException;

    /**
     * Actualiza los datos de un funcionario existente.
     * @param funcionario Objeto con los datos actualizados.
     * @return true si se actualizo con exito.
     * @throws SQLException Si ocurre un error en la actualizacion.
     */
    boolean actualizar(Funcionario funcionario) throws SQLException;

    /**
     * Elimina un funcionario por su ID.
     * @param idFuncionario ID del funcionario a eliminar.
     * @return true si se elimino con exito.
     * @throws SQLException Si ocurre un error o restriccion de clave foranea.
     */
    boolean eliminar(int idFuncionario) throws SQLException;

    /**
     * Comprueba si un numero de documento ya existe en el sistema.
     * @param numeroDocumento Documento a verificar.
     * @param excluirId ID a excluir (para cuando se esta editando el mismo registro), o null si es registro nuevo.
     * @return true si el documento ya esta registrado por otro funcionario.
     * @throws SQLException Si ocurre un error en la consulta.
     */
    boolean existeDocumento(String numeroDocumento, Integer excluirId) throws SQLException;
}
