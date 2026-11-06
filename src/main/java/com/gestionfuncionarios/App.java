package com.gestionfuncionarios;

import com.gestionfuncionarios.vista.VentanaLogin;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal de la aplicacion de Gestion de Funcionarios.
 * Muestra la ventana de autenticacion (Login) antes de permitir el acceso
 * a la ventana principal. La apertura de la ventana principal queda
 * condicionada a la validacion exitosa de credenciales en VentanaLogin.
 */
public class App {

    public static void main(String[] args) {
        // Ejecucion segura en el Event Dispatch Thread (EDT) de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                VentanaLogin login = new VentanaLogin();
                login.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error critico al iniciar la aplicacion: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}

