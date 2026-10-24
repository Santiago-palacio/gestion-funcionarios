package com.gestionfuncionarios;

import com.gestionfuncionarios.vista.VentanaFuncionarios;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal de la aplicacion de Gestion de Funcionarios.
 */
public class App {

    public static void main(String[] args) {
        // Ejecucion segura en el Event Dispatch Thread (EDT) de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                VentanaFuncionarios ventana = new VentanaFuncionarios();
                ventana.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error critico al iniciar la aplicacion: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
