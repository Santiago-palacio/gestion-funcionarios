package com.gestionfuncionarios.conexion;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestor seguro de conexion a la base de datos MySQL mediante JDBC.
 * Permite configurar las credenciales mediante variables de entorno o
 * archivo externo 'config.properties' para evitar almacenar credenciales
 * directamente en el codigo fuente.
 */
public class ConexionBD {

    private static final String DEFAULT_URL = 
            "jdbc:mysql://localhost:3306/gestion_funcionarios?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";
    private static final String DEFAULT_USER = "root";

    private static String url;
    private static String usuario;
    private static String contrasena;
    private static boolean configurado = false;

    // Inicializacion y carga de configuracion
    private static synchronized void cargarConfiguracion() {
        if (configurado) {
            return;
        }

        // 1. Intentar cargar desde archivo config.properties (en directorio actual, directorio padre o classpath)
        Properties props = new Properties();
        boolean archivoCargado = false;

        String[] posiblesRutas = {
            "config.properties",
            "../config.properties",
            "gestion-funcionarios/config.properties"
        };

        for (String ruta : posiblesRutas) {
            File archivo = new File(ruta);
            if (archivo.exists() && archivo.isFile()) {
                try (FileInputStream fis = new FileInputStream(archivo)) {
                    props.load(fis);
                    archivoCargado = true;
                    break;
                } catch (Exception ignored) {
                }
            }
        }

        if (!archivoCargado) {
            try (InputStream is = ConexionBD.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (is != null) {
                    props.load(is);
                    archivoCargado = true;
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Prioridad a variables de entorno, luego propiedades de archivo, luego defaults
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass == null) {
            envPass = System.getenv("MYSQL_PWD");
        }

        url = (envUrl != null && !envUrl.isBlank()) ? envUrl : props.getProperty("db.url", DEFAULT_URL);
        usuario = (envUser != null && !envUser.isBlank()) ? envUser : props.getProperty("db.user", DEFAULT_USER);
        contrasena = (envPass != null) ? envPass : props.getProperty("db.password", "");

        // 3. Registrar el driver de MySQL explicitamente
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Advertencia: No se encontro el driver com.mysql.cj.jdbc.Driver en el classpath.");
        }

        configurado = true;
    }

    /**
     * Obtiene una nueva conexion activa con la base de datos MySQL.
     * Debe ser cerrada mediante try-with-resources por la clase que la invoque.
     * 
     * @return Connection abierta
     * @throws SQLException si las credenciales o el servidor no estan disponibles
     */
    public static Connection conectar() throws SQLException {
        cargarConfiguracion();
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    /**
     * Informacion publica de depuracion sin exponer contrasenas.
     */
    public static String getInfoConexion() {
        cargarConfiguracion();
        return "URL: " + url + " | Usuario: " + usuario + " | Contrasena: [PROTEGIDA]";
    }

    /**
     * Permite forzar la recarga de configuracion en tiempo de ejecucion si es necesario.
     */
    public static synchronized void recargarConfiguracion() {
        configurado = false;
        cargarConfiguracion();
    }
}
