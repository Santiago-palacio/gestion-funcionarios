package com.gestionfuncionarios.vista;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Validador de entradas para funcionarios, grupo familiar y estudios.
 * Aplica principios de desarrollo de software seguro:
 * - Validacion de presencia (campos obligatorios).
 * - Restricciones de longitud para prevenir desbordamientos o errores SQL.
 * - Listas blancas (whitelisting) de caracteres mediante expresiones regulares.
 * - Validacion estricta de fechas cronologicas y biologicas coherentes.
 */
public class ValidadorFuncionario {

    // Formateador estricto para AAAA-MM-DD
    private static final DateTimeFormatter FORMATO_FECHA = 
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    public static class ResultadoValidacion {
        private final boolean valido;
        private final String mensajeError;

        public ResultadoValidacion(boolean valido, String mensajeError) {
            this.valido = valido;
            this.mensajeError = mensajeError;
        }

        public boolean isValido() {
            return valido;
        }

        public String getMensajeError() {
            return mensajeError;
        }
    }

    /**
     * Valida los datos ingresados en el formulario de funcionarios.
     */
    public static ResultadoValidacion validar(String tipoDoc, String numDoc, String nombres,
                                              String apellidos, String fechaNacimientoTexto,
                                              String telefono, String direccion) {

        if (tipoDoc == null || tipoDoc.trim().isEmpty()) {
            return new ResultadoValidacion(false, "El tipo de documento es obligatorio.");
        }

        if (tipoDoc.trim().length() > 20) {
            return new ResultadoValidacion(false, "El tipo de documento no puede exceder 20 caracteres.");
        }

        if (numDoc == null || numDoc.trim().isEmpty()) {
            return new ResultadoValidacion(false, "El numero de documento es obligatorio.");
        }

        String numLimpio = numDoc.trim();
        if (numLimpio.length() < 3 || numLimpio.length() > 20) {
            return new ResultadoValidacion(false, "El numero de documento debe tener entre 3 y 20 caracteres.");
        }

        if (!numLimpio.matches("^[a-zA-Z0-9-]+$")) {
            return new ResultadoValidacion(false, "El numero de documento solo puede contener letras, numeros o guiones.");
        }

        if (nombres == null || nombres.trim().isEmpty()) {
            return new ResultadoValidacion(false, "Los nombres son obligatorios.");
        }

        if (nombres.trim().length() > 100) {
            return new ResultadoValidacion(false, "Los nombres no pueden exceder 100 caracteres.");
        }

        if (apellidos == null || apellidos.trim().isEmpty()) {
            return new ResultadoValidacion(false, "Los apellidos son obligatorios.");
        }

        if (apellidos.trim().length() > 100) {
            return new ResultadoValidacion(false, "Los apellidos no pueden exceder 100 caracteres.");
        }

        if (direccion != null && direccion.trim().length() > 200) {
            return new ResultadoValidacion(false, "La direccion no puede exceder 200 caracteres.");
        }

        if (telefono != null && !telefono.trim().isEmpty()) {
            if (telefono.trim().length() > 20) {
                return new ResultadoValidacion(false, "El telefono no puede exceder 20 caracteres.");
            }
            if (!telefono.trim().matches("^[0-9+() -]+$")) {
                return new ResultadoValidacion(false, "El telefono contiene caracteres no validos (solo digitos, +, -, () o espacios).");
            }
        }

        if (fechaNacimientoTexto != null && !fechaNacimientoTexto.trim().isEmpty()) {
            ResultadoValidacion resFecha = validarFechaNacimiento(fechaNacimientoTexto.trim());
            if (!resFecha.isValido()) {
                return resFecha;
            }
        }

        return new ResultadoValidacion(true, null);
    }

    /**
     * Valida de forma estricta una fecha de nacimiento.
     */
    public static ResultadoValidacion validarFechaNacimiento(String fechaTexto) {
        if (fechaTexto == null || fechaTexto.trim().isEmpty()) {
            return new ResultadoValidacion(true, null);
        }

        try {
            LocalDate fecha = LocalDate.parse(fechaTexto.trim(), FORMATO_FECHA);
            if (fecha.isAfter(LocalDate.now())) {
                return new ResultadoValidacion(false, "La fecha de nacimiento no puede ser futura.");
            }
            if (fecha.isBefore(LocalDate.now().minusYears(120))) {
                return new ResultadoValidacion(false, "La fecha de nacimiento no parece valida (mas de 120 anos).");
            }
        } catch (DateTimeParseException e) {
            return new ResultadoValidacion(false, "La fecha de nacimiento debe ser valida y tener el formato AAAA-MM-DD (Ejemplo: 1990-05-25).");
        }

        return new ResultadoValidacion(true, null);
    }

    /**
     * Valida los datos de un familiar antes de registrarlo.
     */
    public static ResultadoValidacion validarFamiliar(String nombres, String apellidos,
                                                      String parentesco, String fechaNacTexto) {
        if (nombres == null || nombres.trim().isEmpty()) {
            return new ResultadoValidacion(false, "Los nombres del familiar son obligatorios.");
        }
        if (nombres.trim().length() > 100) {
            return new ResultadoValidacion(false, "Los nombres del familiar no pueden exceder 100 caracteres.");
        }

        if (apellidos == null || apellidos.trim().isEmpty()) {
            return new ResultadoValidacion(false, "Los apellidos del familiar son obligatorios.");
        }
        if (apellidos.trim().length() > 100) {
            return new ResultadoValidacion(false, "Los apellidos del familiar no pueden exceder 100 caracteres.");
        }

        if (parentesco == null || parentesco.trim().isEmpty()) {
            return new ResultadoValidacion(false, "El parentesco es obligatorio.");
        }
        if (parentesco.trim().length() > 50) {
            return new ResultadoValidacion(false, "El parentesco no puede exceder 50 caracteres.");
        }

        if (fechaNacTexto != null && !fechaNacTexto.trim().isEmpty()) {
            ResultadoValidacion resFecha = validarFechaNacimiento(fechaNacTexto.trim());
            if (!resFecha.isValido()) {
                return resFecha;
            }
        }

        return new ResultadoValidacion(true, null);
    }

    /**
     * Valida los datos de un estudio academico antes de registrarlo.
     */
    public static ResultadoValidacion validarEstudio(String universidad, String nivelAcademico, String tituloObtenido) {
        if (universidad == null || universidad.trim().isEmpty()) {
            return new ResultadoValidacion(false, "La universidad o institucion es obligatoria.");
        }
        if (universidad.trim().length() > 150) {
            return new ResultadoValidacion(false, "La institucion no puede exceder 150 caracteres.");
        }

        if (nivelAcademico == null || nivelAcademico.trim().isEmpty()) {
            return new ResultadoValidacion(false, "El nivel academico es obligatorio.");
        }
        if (nivelAcademico.trim().length() > 100) {
            return new ResultadoValidacion(false, "El nivel academico no puede exceder 100 caracteres.");
        }

        if (tituloObtenido != null && tituloObtenido.trim().length() > 150) {
            return new ResultadoValidacion(false, "El titulo obtenido no puede exceder 150 caracteres.");
        }

        return new ResultadoValidacion(true, null);
    }

    /**
     * Convierte de manera segura texto AAAA-MM-DD a LocalDate. Retorna null si el texto es vacio o invalido.
     */
    public static LocalDate parsearFecha(String fechaTexto) {
        if (fechaTexto == null || fechaTexto.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(fechaTexto.trim(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
