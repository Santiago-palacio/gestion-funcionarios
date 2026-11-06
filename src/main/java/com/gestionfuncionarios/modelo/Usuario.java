package com.gestionfuncionarios.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa a un usuario autenticable del sistema.
 * 
 * Regla de seguridad:
 * - Nunca almacena contraseñas en texto plano.
 * - El metodo toString() enmascara u omite el passwordHash para evitar
 *   exposicion accidental en logs o mensajes de consola.
 */
public class Usuario {

    private Integer idUsuario;
    private String username;
    private String passwordHash;
    private Rol rol;
    private boolean activo;
    private LocalDateTime fechaCreacion;

    public Usuario() {
        this.activo = true;
    }

    public Usuario(Integer idUsuario, String username, String passwordHash, Rol rol, boolean activo, LocalDateTime fechaCreacion) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    public Usuario(String username, String passwordHash, Rol rol, boolean activo) {
        this(null, username, passwordHash, rol, activo, null);
    }

    public Usuario(String username, String passwordHash, Rol rol) {
        this(null, username, passwordHash, rol, true, null);
    }

    // Getters y Setters
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean esAdministrador() {
        return this.rol == Rol.ADMINISTRADOR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(idUsuario, usuario.idUsuario) ||
               (username != null && username.equalsIgnoreCase(usuario.username));
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, (username != null ? username.toLowerCase() : null));
    }

    @Override
    public String toString() {
        // Enmascaramiento estricto de credenciales en representacion textual
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", username='" + username + '\'' +
                ", rol=" + rol +
                ", activo=" + activo +
                ", passwordHash='[PROTEGIDO]'" +
                '}';
    }
}

