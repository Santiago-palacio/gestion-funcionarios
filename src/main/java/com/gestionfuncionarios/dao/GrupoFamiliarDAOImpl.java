package com.gestionfuncionarios.dao;

import com.gestionfuncionarios.conexion.ConexionBD;
import com.gestionfuncionarios.modelo.GrupoFamiliar;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion JDBC del DAO de Grupo Familiar.
 */
public class GrupoFamiliarDAOImpl implements GrupoFamiliarDAO {

    @Override
    public List<GrupoFamiliar> listarPorFuncionario(int idFuncionario) throws SQLException {
        List<GrupoFamiliar> lista = new ArrayList<>();
        String sql = "SELECT id_familiar, id_funcionario, nombres, apellidos, parentesco, fecha_nacimiento "
                   + "FROM grupo_familiar WHERE id_funcionario = ? ORDER BY id_familiar ASC";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idFuncionario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GrupoFamiliar gf = new GrupoFamiliar();
                    gf.setIdFamiliar(rs.getInt("id_familiar"));
                    gf.setIdFuncionario(rs.getInt("id_funcionario"));
                    gf.setNombres(rs.getString("nombres"));
                    gf.setApellidos(rs.getString("apellidos"));
                    gf.setParentesco(rs.getString("parentesco"));

                    Date fecha = rs.getDate("fecha_nacimiento");
                    if (fecha != null) {
                        gf.setFechaNacimiento(fecha.toLocalDate());
                    }
                    lista.add(gf);
                }
            }
        }
        return lista;
    }

    @Override
    public boolean registrar(GrupoFamiliar familiar) throws SQLException {
        if (familiar == null) {
            throw new IllegalArgumentException("El familiar no puede ser nulo.");
        }

        String sql = "INSERT INTO grupo_familiar (id_funcionario, nombres, apellidos, parentesco, fecha_nacimiento) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, familiar.getIdFuncionario());
            ps.setString(2, familiar.getNombres());
            ps.setString(3, familiar.getApellidos());
            ps.setString(4, familiar.getParentesco());

            if (familiar.getFechaNacimiento() != null) {
                ps.setDate(5, Date.valueOf(familiar.getFechaNacimiento()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        familiar.setIdFamiliar(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean eliminar(int idFamiliar) throws SQLException {
        String sql = "DELETE FROM grupo_familiar WHERE id_familiar = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idFamiliar);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int eliminarPorFuncionario(int idFuncionario) throws SQLException {
        String sql = "DELETE FROM grupo_familiar WHERE id_funcionario = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idFuncionario);
            return ps.executeUpdate();
        }
    }
}
