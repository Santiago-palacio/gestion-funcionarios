package com.gestionfuncionarios.seguridad;

import com.gestionfuncionarios.modelo.Rol;
import com.gestionfuncionarios.modelo.Usuario;

/**
 * Gestor centralizado en memoria de la sesion del usuario autenticado (Patron Singleton).
 * 
 * Responsabilidades:
 * - Almacenar el usuario autenticado durante la ejecucion de la aplicacion.
 * - Permitir la consulta global del rol y privilegios de la sesion activa.
 * - Garantizar la destruccion segura de la sesion al invocar cerrarSesion().
 */
public final class SesionUsuario {

    private static volatile SesionUsuario instancia;
    private Usuario usuarioActual;

    private SesionUsuario() {
        this.usuarioActual = null;
    }

    public static SesionUsuario getInstancia() {
        if (instancia == null) {
            synchronized (SesionUsuario.class) {
                if (instancia == null) {
                    instancia = new SesionUsuario();
                }
            }
        }
        return instancia;
    }

    /**
     * Establece el usuario autenticado para la sesion actual.
     */
    public synchronized void iniciarSesion(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("No se puede iniciar sesion con un usuario nulo.");
        }
        if (!usuario.isActivo()) {
            throw new AccesoDenegadoException("No se puede iniciar sesion con una cuenta de usuario inactiva.");
        }
        this.usuarioActual = usuario;
    }

    /**
     * Cierra la sesion activa y limpia las referencias en memoria.
     */
    public synchronized void cerrarSesion() {
        this.usuarioActual = null;
    }

    /**
     * Comprueba si existe una sesion valida y activa.
     */
    public synchronized boolean haySesionActiva() {
        return this.usuarioActual != null && this.usuarioActual.isActivo();
    }

    /**
     * Retorna el usuario autenticado actualmente.
     */
    public synchronized Usuario getUsuarioActual() {
        return this.usuarioActual;
    }

    /**
     * Retorna el rol del usuario actualmente autenticado, o null si no hay sesion.
     */
    public synchronized Rol getRolActual() {
        return (this.usuarioActual != null) ? this.usuarioActual.getRol() : null;
    }

    /**
     * Indica si el usuario en sesion posee privilegios de Administrador.
     */
    public synchronized boolean esAdministrador() {
        return haySesionActiva() && this.usuarioActual.getRol() == Rol.ADMINISTRADOR;
    }

    /**
     * Indica si el usuario en sesion posee el rol de Consulta.
     */
    public synchronized boolean esConsulta() {
        return haySesionActiva() && this.usuarioActual.getRol() == Rol.CONSULTA;
    }

    /**
     * Retorna el nombre del usuario activo o "Invitado" si no hay sesion.
     */
    public synchronized String getUsernameActual() {
        return (this.usuarioActual != null) ? this.usuarioActual.getUsername() : "Invitado";
    }
}

