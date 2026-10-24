package com.gestionfuncionarios.dao;

import com.gestionfuncionarios.modelo.GrupoFamiliar;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz DAO para operaciones de persistencia del Grupo Familiar.
 */
public interface GrupoFamiliarDAO {

    /**
     * Obtiene todos los familiares vinculados a un funcionario.
     * @param idFuncionario ID del funcionario.
     * @return Lista de familiares.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    List<GrupoFamiliar> listarPorFuncionario(int idFuncionario) throws SQLException;

    /**
     * Registra un nuevo familiar para un funcionario.
     * @param familiar Objeto con los datos del familiar.
     * @return true si se registro correctamente.
     * @throws SQLException Si ocurre un error.
     */
    boolean registrar(GrupoFamiliar familiar) throws SQLException;

    /**
     * Elimina un familiar por su ID.
     * @param idFamiliar ID del familiar.
     * @return true si se elimino correctamente.
     * @throws SQLException Si ocurre un error.
     */
    boolean eliminar(int idFamiliar) throws SQLException;

    /**
     * Elimina todos los familiares de un funcionario (util si se desea eliminar en cascada controlada).
     * @param idFuncionario ID del funcionario.
     * @return Cantidad de registros eliminados.
     * @throws SQLException Si ocurre un error.
     */
    int eliminarPorFuncionario(int idFuncionario) throws SQLException;
}
