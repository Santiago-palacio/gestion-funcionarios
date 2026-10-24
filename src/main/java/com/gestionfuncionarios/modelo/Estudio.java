package com.gestionfuncionarios.modelo;

import java.util.Objects;

/**
 * Entidad que representa la formacion academica de un Funcionario.
 */
public class Estudio {

    private Integer idEstudio;
    private Integer idFuncionario;
    private String universidad;
    private String nivelAcademico;
    private String tituloObtenido;

    public Estudio() {
    }

    public Estudio(Integer idEstudio, Integer idFuncionario, String universidad,
                   String nivelAcademico, String tituloObtenido) {
        this.idEstudio = idEstudio;
        this.idFuncionario = idFuncionario;
        this.universidad = universidad;
        this.nivelAcademico = nivelAcademico;
        this.tituloObtenido = tituloObtenido;
    }

    public Estudio(Integer idFuncionario, String universidad,
                   String nivelAcademico, String tituloObtenido) {
        this(null, idFuncionario, universidad, nivelAcademico, tituloObtenido);
    }

    public Integer getIdEstudio() {
        return idEstudio;
    }

    public void setIdEstudio(Integer idEstudio) {
        this.idEstudio = idEstudio;
    }

    public Integer getIdFuncionario() {
        return idFuncionario;
    }

    public void setIdFuncionario(Integer idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public String getUniversidad() {
        return universidad;
    }

    public void setUniversidad(String universidad) {
        this.universidad = universidad;
    }

    public String getNivelAcademico() {
        return nivelAcademico;
    }

    public void setNivelAcademico(String nivelAcademico) {
        this.nivelAcademico = nivelAcademico;
    }

    public String getTituloObtenido() {
        return tituloObtenido;
    }

    public void setTituloObtenido(String tituloObtenido) {
        this.tituloObtenido = tituloObtenido;
    }

    @Override
    public String toString() {
        return tituloObtenido + " (" + universidad + " - " + nivelAcademico + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estudio estudio = (Estudio) o;
        return Objects.equals(idEstudio, estudio.idEstudio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEstudio);
    }
}
