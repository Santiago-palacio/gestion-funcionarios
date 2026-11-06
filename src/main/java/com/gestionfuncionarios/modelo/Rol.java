package com.gestionfuncionarios.modelo;

/**
 * Enumeracion que define los roles de seguridad y control de acceso del sistema.
 */
public enum Rol {

    ADMINISTRADOR(1, "ADMINISTRADOR", "Acceso total: consulta, registro, modificacion y eliminacion de funcionarios y relaciones"),
    CONSULTA(2, "CONSULTA", "Acceso de solo lectura: visualizacion de funcionarios, familiares y estudios sin privilegios de modificacion");

    private final int idRol;
    private final String nombre;
    private final String descripcion;

    Rol(int idRol, String nombre, String descripcion) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getIdRol() {
        return idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene el Rol correspondiente a partir de su ID numerico en base de datos.
     */
    public static Rol fromId(int idRol) {
        for (Rol r : values()) {
            if (r.idRol == idRol) {
                return r;
            }
        }
        throw new IllegalArgumentException("No existe ningun Rol con id: " + idRol);
    }

    /**
     * Obtiene el Rol correspondiente a partir de su nombre textual.
     */
    public static Rol fromNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del rol no puede ser nulo ni vacio.");
        }
        for (Rol r : values()) {
            if (r.nombre.equalsIgnoreCase(nombre.trim())) {
                return r;
            }
        }
        throw new IllegalArgumentException("Rol desconocido: " + nombre);
    }

    @Override
    public String toString() {
        return nombre;
    }
}

