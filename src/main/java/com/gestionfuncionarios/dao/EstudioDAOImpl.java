package com.gestionfuncionarios.dao;

import com.gestionfuncionarios.conexion.ConexionBD;
import com.gestionfuncionarios.modelo.Estudio;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion JDBC del DAO de Estudios Academicos.
 */
public class EstudioDAOImpl implements EstudioDAO {

    @Override
    public List<Estudio> listarPorFuncionario(int idFuncionario) throws SQLException {
        List<Estudio> lista = new ArrayList<>();
        String sql = "SELECT id_estudio, id_funcionario, universidad, nivel_academico, titulo_obtenido "
                   + "FROM estudios WHERE id_funcionario = ? ORDER BY id_estudio ASC";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idFuncionario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Estudio e = new Estudio();
                    e.setIdEstudio(rs.getInt("id_estudio"));
                    e.setIdFuncionario(rs.getInt("id_funcionario"));
                    e.setUniversidad(rs.getString("universidad"));
                    e.setNivelAcademico(rs.getString("nivel_academico"));
                    e.setTituloObtenido(rs.getString("titulo_obtenido"));
                    lista.add(e);
                }
            }
        }
        return lista;
    }

    @Override
    public boolean registrar(Estudio estudio) throws SQLException {
        if (estudio == null) {
            throw new IllegalArgumentException("El estudio no puede ser nulo.");
        }

        String sql = "INSERT INTO estudios (id_funcionario, universidad, nivel_academico, titulo_obtenido) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, estudio.getIdFuncionario());
            ps.setString(2, estudio.getUniversidad());
            ps.setString(3, estudio.getNivelAcademico());
            ps.setString(4, estudio.getTituloObtenido());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        estudio.setIdEstudio(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean eliminar(int idEstudio) throws SQLException {
        String sql = "DELETE FROM estudios WHERE id_estudio = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEstudio);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int eliminarPorFuncionario(int idFuncionario) throws SQLException {
        String sql = "DELETE FROM estudios WHERE id_funcionario = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idFuncionario);
            return ps.executeUpdate();
        }
    }
}
