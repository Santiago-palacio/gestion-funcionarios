package com.gestionfuncionarios.seguridad;

/**
 * Excepcion de seguridad lanzada cuando un usuario autenticado intenta ejecutar
 * una operacion para la cual su rol no tiene los privilegios requeridos.
 * 
 * Permite garantizar el control de acceso en la capa de logica de negocio
 * independientemente de las protecciones visuales de la interfaz de usuario.
 */
public class AccesoDenegadoException extends SecurityException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }

    public AccesoDenegadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

