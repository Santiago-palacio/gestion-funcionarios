package com.gestionfuncionarios.app;

import com.gestionfuncionarios.conexion.ConexionBD;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utilidad de diagnostico para comprobar la conexion JDBC y el estado
 * de las tablas en la base de datos gestion_funcionarios.
 */
public class PruebaConexion {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" DIAGNOSTICO DE CONEXION JDBC - MYSQL");
        System.out.println("==================================================");
        System.out.println("Parametros: " + ConexionBD.getInfoConexion());

        try (Connection conexion = ConexionBD.conectar()) {
            DatabaseMetaData meta = conexion.getMetaData();
            System.out.println("\n[OK] ¡Conexion establecida exitosamente con MySQL!");
            System.out.println("Motor de base de datos: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            System.out.println("Driver JDBC: " + meta.getDriverName() + " " + meta.getDriverVersion());
            System.out.println("Catalogo actual: " + conexion.getCatalog());

            // Comprobacion de tablas existentes
            verificarTabla(conexion, "funcionarios");
            verificarTabla(conexion, "grupo_familiar");
            verificarTabla(conexion, "estudios");

            System.out.println("\n==================================================");
            System.out.println(" DIAGNOSTICO FINALIZADO CORRECTAMENTE");
            System.out.println("==================================================");

        } catch (SQLException e) {
            System.err.println("\n[ERROR] No se pudo establecer conexion con la base de datos.");
            System.err.println("Codigo de error SQL: " + e.getErrorCode());
            System.err.println("Estado SQL: " + e.getSQLState());
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("\nRecomendaciones:");
            System.err.println("1. Asegurate de que el servicio MySQL84 este iniciado en Windows.");
            System.err.println("2. Revisa las credenciales en 'config.properties' o en la variable de entorno DB_PASSWORD.");
            System.err.println("3. Comprueba que la base de datos 'gestion_funcionarios' exista.");
        }
    }

    private static void verificarTabla(Connection conn, String tabla) {
        String sql = "SELECT COUNT(*) FROM " + tabla;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println(" - Tabla '" + tabla + "': OK (" + count + " registro(s) encontrados).");
            }
        } catch (SQLException e) {
            System.out.println(" - Tabla '" + tabla + "': ERROR (" + e.getMessage() + ")");
        }
    }
}
