package com.gestionfuncionarios.dao;

import com.gestionfuncionarios.conexion.ConexionBD;
import com.gestionfuncionarios.modelo.Funcionario;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion JDBC del DAO de Funcionarios.
 * Aplica principios de desarrollo seguro: consultas parametrizadas,
 * try-with-resources y gestion de excepciones SQL especificas.
 */
public class FuncionarioDAOImpl implements FuncionarioDAO {

    @Override
    public boolean registrar(Funcionario funcionario) throws SQLException {
        if (funcionario == null) {
            throw new IllegalArgumentException("El funcionario no puede ser nulo.");
        }

        String sql = "INSERT INTO funcionarios "
                   + "(tipo_documento, numero_documento, nombres, apellidos, estado_civil, sexo, direccion, telefono, fecha_nacimiento) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, funcionario.getTipoDocumento());
            ps.setString(2, funcionario.getNumeroDocumento());
            ps.setString(3, funcionario.getNombres());
            ps.setString(4, funcionario.getApellidos());
            ps.setString(5, funcionario.getEstadoCivil());
            ps.setString(6, funcionario.getSexo());
            ps.setString(7, funcionario.getDireccion());
            ps.setString(8, funcionario.getTelefono());

            if (funcionario.getFechaNacimiento() != null) {
                ps.setDate(9, Date.valueOf(funcionario.getFechaNacimiento()));
            } else {
                ps.setNull(9, Types.DATE);
            }

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        funcionario.setIdFuncionario(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            // Codigo 1062: ER_DUP_ENTRY (Entrada duplicada para numero_documento)
            if (e.getErrorCode() == 1062) {
                throw new SQLException("El numero de documento '" + funcionario.getNumeroDocumento() 
                        + "' ya se encuentra registrado en el sistema.", e);
            }
            throw e;
        }
    }

    @Override
    public List<Funcionario> listarTodos() throws SQLException {
        List<Funcionario> lista = new ArrayList<>();
        String sql = "SELECT id_funcionario, tipo_documento, numero_documento, nombres, apellidos, "
                   + "estado_civil, sexo, direccion, telefono, fecha_nacimiento "
                   + "FROM funcionarios ORDER BY id_funcionario ASC";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearFuncionario(rs));
            }
        }
        return lista;
    }

    @Override
    public Funcionario buscarPorId(int idFuncionario) throws SQLException {
        String sql = "SELECT id_funcionario, tipo_documento, numero_documento, nombres, apellidos, "
                   + "estado_civil, sexo, direccion, telefono, fecha_nacimiento "
                   + "FROM funcionarios WHERE id_funcionario = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idFuncionario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearFuncionario(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Funcionario buscarPorDocumento(String numeroDocumento) throws SQLException {
        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            return null;
        }

        String sql = "SELECT id_funcionario, tipo_documento, numero_documento, nombres, apellidos, "
                   + "estado_civil, sexo, direccion, telefono, fecha_nacimiento "
                   + "FROM funcionarios WHERE numero_documento = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numeroDocumento.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearFuncionario(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean actualizar(Funcionario funcionario) throws SQLException {
        if (funcionario == null || funcionario.getIdFuncionario() == null) {
            throw new IllegalArgumentException("El funcionario y su ID son obligatorios para actualizar.");
        }

        String sql = "UPDATE funcionarios SET "
                   + "tipo_documento = ?, numero_documento = ?, nombres = ?, apellidos = ?, "
                   + "estado_civil = ?, sexo = ?, direccion = ?, telefono = ?, fecha_nacimiento = ? "
                   + "WHERE id_funcionario = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, funcionario.getTipoDocumento());
            ps.setString(2, funcionario.getNumeroDocumento());
            ps.setString(3, funcionario.getNombres());
            ps.setString(4, funcionario.getApellidos());
            ps.setString(5, funcionario.getEstadoCivil());
            ps.setString(6, funcionario.getSexo());
            ps.setString(7, funcionario.getDireccion());
            ps.setString(8, funcionario.getTelefono());

            if (funcionario.getFechaNacimiento() != null) {
                ps.setDate(9, Date.valueOf(funcionario.getFechaNacimiento()));
            } else {
                ps.setNull(9, Types.DATE);
            }

            ps.setInt(10, funcionario.getIdFuncionario());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new SQLException("El numero de documento '" + funcionario.getNumeroDocumento() 
                        + "' ya pertenece a otro funcionario registrado.", e);
            }
            throw e;
        }
    }

    @Override
    public boolean eliminar(int idFuncionario) throws SQLException {
        String sql = "DELETE FROM funcionarios WHERE id_funcionario = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idFuncionario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Codigo 1451: ER_ROW_IS_REFERENCED_2 (Clave foranea en grupo_familiar o estudios)
            if (e.getErrorCode() == 1451) {
                throw new SQLException("No se puede eliminar el funcionario porque tiene familiares o estudios registrados vinculados.\n"
                        + "Para mantener la integridad referencial, elimine primero sus familiares y estudios asociados.", e);
            }
            throw e;
        }
    }

    @Override
    public boolean existeDocumento(String numeroDocumento, Integer excluirId) throws SQLException {
        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM funcionarios WHERE numero_documento = ?"
                   + (excluirId != null ? " AND id_funcionario <> ?" : "");

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numeroDocumento.trim());
            if (excluirId != null) {
                ps.setInt(2, excluirId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Mapea un ResultSet a un objeto Funcionario.
     */
    private Funcionario mapearFuncionario(ResultSet rs) throws SQLException {
        Funcionario f = new Funcionario();
        f.setIdFuncionario(rs.getInt("id_funcionario"));
        f.setTipoDocumento(rs.getString("tipo_documento"));
        f.setNumeroDocumento(rs.getString("numero_documento"));
        f.setNombres(rs.getString("nombres"));
        f.setApellidos(rs.getString("apellidos"));
        f.setEstadoCivil(rs.getString("estado_civil"));
        f.setSexo(rs.getString("sexo"));
        f.setDireccion(rs.getString("direccion"));
        f.setTelefono(rs.getString("telefono"));

        Date fechaSql = rs.getDate("fecha_nacimiento");
        if (fechaSql != null) {
            f.setFechaNacimiento(fechaSql.toLocalDate());
        }

        return f;
    }
}
