package com.gestionfuncionarios.app;

import com.gestionfuncionarios.dao.UsuarioDAO;
import com.gestionfuncionarios.dao.UsuarioDAOImpl;
import com.gestionfuncionarios.modelo.Rol;
import com.gestionfuncionarios.modelo.Usuario;
import com.gestionfuncionarios.seguridad.PasswordHasher;

import java.io.Console;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Herramienta segura de administracion por consola para crear usuarios iniciales
 * (administradores o consulta) con hashing de contraseñas mediante BCrypt.
 * 
 * Permite al desarrollador o administrador registrar credenciales de forma interactiva
 * sin escribir contraseñas en texto plano dentro de scripts SQL o archivos de codigo.
 */
public class CrearPrimerAdmin {

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println(" UTILIDAD DE SEGURIDAD: CREACION DE USUARIO ADMINISTRADOR");
        System.out.println("=========================================================");

        Scanner scanner = new Scanner(System.in);
        Console console = System.console();

        String username;
        char[] passwordChars;

        System.out.print("\nIngrese el nombre de usuario (ej: admin): ");
        username = scanner.nextLine().trim();

        if (username.isBlank()) {
            System.err.println("[ERROR] El nombre de usuario no puede estar vacio.");
            return;
        }

        if (console != null) {
            // Lectura oculta en terminal sin eco de caracteres
            passwordChars = console.readPassword("Ingrese la contraseña del administrador: ");
        } else {
            System.out.print("Ingrese la contraseña del administrador: ");
            passwordChars = scanner.nextLine().toCharArray();
        }

        if (passwordChars == null || passwordChars.length == 0) {
            System.err.println("[ERROR] La contraseña no puede estar vacia.");
            return;
        }

        System.out.println("\nGenerando hash criptografico con BCrypt (coste 12 y sal aleatoria)...");
        String passwordHash = PasswordHasher.hashear(passwordChars);

        System.out.println("Hash generado con exito: " + passwordHash);

        Usuario nuevoAdmin = new Usuario(username, passwordHash, Rol.ADMINISTRADOR, true);
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

        try {
            if (usuarioDAO.existeUsername(username)) {
                System.out.println("\n[AVISO] El usuario '" + username + "' ya existe en MySQL.");
                System.out.print("¿Desea actualizar su contraseña con el nuevo hash? (s/n): ");
                String resp = scanner.nextLine().trim().toLowerCase();
                if ("s".equals(resp) || "si".equals(resp)) {
                    Usuario uExistente = usuarioDAO.buscarPorUsername(username);
                    usuarioDAO.cambiarPassword(uExistente.getIdUsuario(), passwordHash);
                    System.out.println("[OK] ¡Contraseña actualizada exitosamente para '" + username + "'!");
                } else {
                    System.out.println("Operacion cancelada.");
                }
            } else {
                boolean registrado = usuarioDAO.registrar(nuevoAdmin);
                if (registrado) {
                    System.out.println("\n[OK] ¡Usuario administrador registrado exitosamente en MySQL!");
                    System.out.println("ID asignado: " + nuevoAdmin.getIdUsuario());
                    System.out.println("Usuario: " + nuevoAdmin.getUsername());
                    System.out.println("Rol: " + nuevoAdmin.getRol().getNombre());
                } else {
                    System.err.println("[ERROR] No se pudo registrar el usuario en la base de datos.");
                }
            }
        } catch (SQLException e) {
            System.err.println("\n[ERROR DE BASE DE DATOS] " + e.getMessage());
            System.out.println("\nSentencia SQL equivalente generada para insercion manual:");
            System.out.println("INSERT INTO usuarios (username, password_hash, id_rol, activo) VALUES ('"
                    + username + "', '" + passwordHash + "', 1, TRUE);");
        }
    }
}

