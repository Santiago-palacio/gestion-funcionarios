package com.gestionfuncionarios.dao;

import com.gestionfuncionarios.modelo.Estudio;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz DAO para operaciones de persistencia de Estudios Academicos.
 */
public interface EstudioDAO {

    /**
     * Obtiene todos los estudios vinculados a un funcionario.
     * @param idFuncionario ID del funcionario.
     * @return Lista de estudios academicos.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    List<Estudio> listarPorFuncionario(int idFuncionario) throws SQLException;

    /**
     * Registra un nuevo estudio academico para un funcionario.
     * @param estudio Objeto con los datos del estudio.
     * @return true si se registro correctamente.
     * @throws SQLException Si ocurre un error.
     */
    boolean registrar(Estudio estudio) throws SQLException;

    /**
     * Elimina un estudio por su ID.
     * @param idEstudio ID del estudio.
     * @return true si se elimino correctamente.
     * @throws SQLException Si ocurre un error.
     */
    boolean eliminar(int idEstudio) throws SQLException;

    /**
     * Elimina todos los estudios de un funcionario (util si se desea eliminar en cascada controlada).
     * @param idFuncionario ID del funcionario.
     * @return Cantidad de registros eliminados.
     * @throws SQLException Si ocurre un error.
     */
    int eliminarPorFuncionario(int idFuncionario) throws SQLException;
}
