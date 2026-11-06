package com.gestionfuncionarios.dao;

import com.gestionfuncionarios.conexion.ConexionBD;
import com.gestionfuncionarios.modelo.Rol;
import com.gestionfuncionarios.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion JDBC del DAO de Usuarios.
 * Aplica principios de desarrollo seguro:
 * - 100% consultas parametrizadas con PreparedStatement (previene SQL Injection).
 * - Cierre automatico de conexiones y cursores con try-with-resources.
 * - JOIN con tabla roles para resolver permisos de forma consistente.
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    private static final String SELECT_BASE = 
            "SELECT u.id_usuario, u.username, u.password_hash, u.id_rol, u.activo, u.fecha_creacion, " +
            "r.nombre AS nombre_rol, r.descripcion AS descripcion_rol " +
            "FROM usuarios u " +
            "INNER JOIN roles r ON u.id_rol = r.id_rol ";

    @Override
    public Usuario buscarPorUsername(String username) throws SQLException {
        if (username == null || username.isBlank()) {
            return null;
        }

        String sql = SELECT_BASE + "WHERE u.username = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Usuario buscarPorId(int idUsuario) throws SQLException {
        String sql = SELECT_BASE + "WHERE u.id_usuario = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY u.id_usuario ASC";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }

    @Override
    public boolean registrar(Usuario usuario) throws SQLException {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        if (usuario.getRol() == null) {
            throw new IllegalArgumentException("El rol del usuario es obligatorio.");
        }

        String sql = "INSERT INTO usuarios (username, password_hash, id_rol, activo) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getUsername().trim());
            ps.setString(2, usuario.getPasswordHash());
            ps.setInt(3, usuario.getRol().getIdRol());
            ps.setBoolean(4, usuario.isActivo());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setIdUsuario(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new SQLException("El nombre de usuario '" + usuario.getUsername() + "' ya se encuentra registrado.", e);
            }
            throw e;
        }
    }

    @Override
    public boolean actualizarEstado(int idUsuario, boolean activo) throws SQLException {
        String sql = "UPDATE usuarios SET activo = ? WHERE id_usuario = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean cambiarPassword(int idUsuario, String nuevoPasswordHash) throws SQLException {
        if (nuevoPasswordHash == null || nuevoPasswordHash.isBlank()) {
            throw new IllegalArgumentException("El nuevo hash de contraseña no puede estar vacio.");
        }

        String sql = "UPDATE usuarios SET password_hash = ? WHERE id_usuario = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoPasswordHash);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existeUsername(String username) throws SQLException {
        if (username == null || username.isBlank()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean actualizarRol(int idUsuario, int idRol) throws SQLException {
        String sql = "UPDATE usuarios SET id_rol = ? WHERE id_usuario = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean actualizarUsuario(int idUsuario, String username, int idRol, boolean activo) throws SQLException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacio.");
        }

        String sql = "UPDATE usuarios SET username = ?, id_rol = ?, activo = ? WHERE id_usuario = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setInt(2, idRol);
            ps.setBoolean(3, activo);
            ps.setInt(4, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new SQLException("El nombre de usuario '" + username + "' ya esta registrado por otra cuenta.", e);
            }
            throw e;
        }
    }

    @Override
    public int contarAdministradoresActivos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios u " +
                     "INNER JOIN roles r ON u.id_rol = r.id_rol " +
                     "WHERE r.nombre = 'ADMINISTRADOR' AND u.activo = 1";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setActivo(rs.getBoolean("activo"));

        String nombreRol = rs.getString("nombre_rol");
        if (nombreRol != null) {
            u.setRol(Rol.fromNombre(nombreRol));
        } else {
            u.setRol(Rol.fromId(rs.getInt("id_rol")));
        }

        Timestamp ts = rs.getTimestamp("fecha_creacion");
        if (ts != null) {
            u.setFechaCreacion(ts.toLocalDateTime());
        }

        return u;
    }
}

