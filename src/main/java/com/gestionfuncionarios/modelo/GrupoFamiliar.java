package com.gestionfuncionarios.modelo;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa a un miembro del grupo familiar asociado a un Funcionario.
 */
public class GrupoFamiliar {

    private Integer idFamiliar;
    private Integer idFuncionario;
    private String nombres;
    private String apellidos;
    private String parentesco;
    private LocalDate fechaNacimiento;

    public GrupoFamiliar() {
    }

    public GrupoFamiliar(Integer idFamiliar, Integer idFuncionario, String nombres,
                         String apellidos, String parentesco, LocalDate fechaNacimiento) {
        this.idFamiliar = idFamiliar;
        this.idFuncionario = idFuncionario;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.parentesco = parentesco;
        this.fechaNacimiento = fechaNacimiento;
    }

    public GrupoFamiliar(Integer idFuncionario, String nombres, String apellidos,
                         String parentesco, LocalDate fechaNacimiento) {
        this(null, idFuncionario, nombres, apellidos, parentesco, fechaNacimiento);
    }

    public Integer getIdFamiliar() {
        return idFamiliar;
    }

    public void setIdFamiliar(Integer idFamiliar) {
        this.idFamiliar = idFamiliar;
    }

    public Integer getIdFuncionario() {
        return idFuncionario;
    }

    public void setIdFuncionario(Integer idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + parentesco + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrupoFamiliar that = (GrupoFamiliar) o;
        return Objects.equals(idFamiliar, that.idFamiliar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFamiliar);
    }
}
