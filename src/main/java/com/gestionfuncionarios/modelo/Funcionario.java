package com.gestionfuncionarios.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa a un Funcionario dentro de la organizacion.
 */
public class Funcionario {

    private Integer idFuncionario;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String estadoCivil;
    private String sexo;
    private String direccion;
    private String telefono;
    private LocalDate fechaNacimiento;

    // Relaciones 1 a N
    private List<GrupoFamiliar> familiares;
    private List<Estudio> estudios;

    public Funcionario() {
        this.familiares = new ArrayList<>();
        this.estudios = new ArrayList<>();
    }

    public Funcionario(Integer idFuncionario, String tipoDocumento, String numeroDocumento,
                       String nombres, String apellidos, String estadoCivil, String sexo,
                       String direccion, String telefono, LocalDate fechaNacimiento) {
        this.idFuncionario = idFuncionario;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.estadoCivil = estadoCivil;
        this.sexo = sexo;
        this.direccion = direccion;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.familiares = new ArrayList<>();
        this.estudios = new ArrayList<>();
    }

    public Funcionario(String tipoDocumento, String numeroDocumento,
                       String nombres, String apellidos, String estadoCivil, String sexo,
                       String direccion, String telefono, LocalDate fechaNacimiento) {
        this(null, tipoDocumento, numeroDocumento, nombres, apellidos, estadoCivil, sexo, direccion, telefono, fechaNacimiento);
    }

    // Getters y Setters
    public Integer getIdFuncionario() {
        return idFuncionario;
    }

    public void setIdFuncionario(Integer idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
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

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public List<GrupoFamiliar> getFamiliares() {
        return familiares;
    }

    public void setFamiliares(List<GrupoFamiliar> familiares) {
        this.familiares = (familiares != null) ? familiares : new ArrayList<>();
    }

    public List<Estudio> getEstudios() {
        return estudios;
    }

    public void setEstudios(List<Estudio> estudios) {
        this.estudios = (estudios != null) ? estudios : new ArrayList<>();
    }

    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + tipoDocumento + ": " + numeroDocumento + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Funcionario that = (Funcionario) o;
        return Objects.equals(idFuncionario, that.idFuncionario) ||
               Objects.equals(numeroDocumento, that.numeroDocumento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFuncionario, numeroDocumento);
    }
}
