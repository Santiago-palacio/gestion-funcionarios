package com.gestionfuncionarios.seguridad;

import com.gestionfuncionarios.modelo.Rol;

/**
 * Componente centralizado de autorizacion (RBAC - Role-Based Access Control).
 * 
 * Garantiza que las politicas de seguridad y permisos se apliquen en la
 * capa de logica de negocio, impidiendo acciones no autorizadas incluso si
 * la interfaz grafica fuera manipulada o si se invocaran las operaciones
 * directamente desde codigo.
 */
public final class ControlAcceso {

    private ControlAcceso() {
        // Clase utilitaria estatica
    }

    /**
     * Valida que exista una sesion activa autenticada en el sistema.
     * 
     * @throws AccesoDenegadoException Si no hay ninguna sesion iniciada.
     */
    public static void validarAutenticacion() {
        if (!SesionUsuario.getInstancia().haySesionActiva()) {
            throw new AccesoDenegadoException("Operacion rechazada: No existe una sesion de usuario activa.");
        }
    }

    /**
     * Valida que el usuario actual tenga permisos de escritura (ADMINISTRADOR).
     * Si el usuario autenticado tiene rol CONSULTA o no hay sesion, lanza AccesoDenegadoException.
     * 
     * @param nombreOperacion Descripcion de la accion intentada (ej. 'Registrar funcionario', 'Eliminar estudio')
     * @throws AccesoDenegadoException Si el rol no tiene privilegios de modificacion
     */
    public static void validarPermisoEscritura(String nombreOperacion) {
        validarAutenticacion();

        if (!SesionUsuario.getInstancia().esAdministrador()) {
            Rol rolActual = SesionUsuario.getInstancia().getRolActual();
            String rolNombre = (rolActual != null) ? rolActual.getNombre() : "DESCONOCIDO";
            throw new AccesoDenegadoException(
                    "Acceso denegado: La operacion '" + nombreOperacion + "' requiere privilegios de ADMINISTRADOR.\n" +
                    "El rol actual '" + rolNombre + "' solo cuenta con permisos de consulta y lectura."
            );
        }
    }

    /**
     * Comprueba de forma booleana si la sesion actual posee permisos para crear,
     * editar o eliminar registros.
     * 
     * @return true si es ADMINISTRADOR con sesion activa
     */
    public static boolean tienePermisoEscritura() {
        return SesionUsuario.getInstancia().esAdministrador();
    }

    /**
     * Comprueba si la sesion actual tiene permisos de visualizacion y consulta.
     * 
     * @return true si hay sesion activa (ADMINISTRADOR o CONSULTA)
     */
    public static boolean tienePermisoLectura() {
        return SesionUsuario.getInstancia().haySesionActiva();
    }
}

