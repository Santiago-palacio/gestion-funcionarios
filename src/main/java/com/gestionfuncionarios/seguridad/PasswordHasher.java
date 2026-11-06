package com.gestionfuncionarios.seguridad;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Arrays;

/**
 * Componente de seguridad encargado del hashing y verificacion de contraseñas
 * mediante el algoritmo criptografico BCrypt.
 * 
 * Principios de seguridad aplicados:
 * - Generacion automatica de sal criptografica aleatoria por cada hash.
 * - Factor de coste computacional (log rounds = 12) para mitigar ataques de fuerza bruta.
 * - Manejo seguro de memoria mediante arreglos char[] con borrado explicito.
 * - No expone contraseñas en logs ni memoria permanente.
 */
public final class PasswordHasher {

    // Factor de costo recomendado por OWASP para BCrypt (2^12 = 4096 iteraciones de derivacion de clave)
    private static final int BCRYPT_WORK_FACTOR = 12;

    private PasswordHasher() {
        // Clase de utilidades estaticas; no instanciable
    }

    /**
     * Genera un hash seguro con sal automatica para una contraseña dada como texto plano.
     * 
     * @param password Contraseña en texto plano
     * @return Hash seguro en formato BCrypt (60 caracteres)
     */
    public static String hashear(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacia para generar el hash.");
        }
        String salt = BCrypt.gensalt(BCRYPT_WORK_FACTOR);
        return BCrypt.hashpw(password, salt);
    }

    /**
     * Sobrecarga de seguridad para generar hash a partir de un arreglo de caracteres char[].
     * Limpia el arreglo en memoria una vez generado el hash.
     * 
     * @param password Caracteres de la contraseña
     * @return Hash seguro en formato BCrypt
     */
    public static String hashear(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("La contraseña no puede estar vacia para generar el hash.");
        }
        String pwdStr = new String(password);
        try {
            return hashear(pwdStr);
        } finally {
            // Mitigacion de fuga en heap memory
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Verifica si una contraseña coincide con el hash BCrypt almacenado.
     * 
     * @param password Contraseña ingresada por el usuario
     * @param hashAlmacenado Hash BCrypt recuperado de la base de datos
     * @return true si la contraseña coincide con el hash, false en caso contrario
     */
    public static boolean verificar(String password, String hashAlmacenado) {
        if (password == null || password.isBlank() || hashAlmacenado == null || hashAlmacenado.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hashAlmacenado);
        } catch (IllegalArgumentException e) {
            // Si el formato del hash no es un hash valido de BCrypt
            return false;
        }
    }

    /**
     * Sobrecarga de seguridad para verificar contraseña desde char[].
     * Limpia el arreglo de caracteres inmediatamente despues de la comparacion.
     * 
     * @param password Caracteres de la contraseña
     * @param hashAlmacenado Hash BCrypt recuperado de la base de datos
     * @return true si coincide, false si no coincide o es invalido
     */
    public static boolean verificar(char[] password, String hashAlmacenado) {
        if (password == null || password.length == 0 || hashAlmacenado == null || hashAlmacenado.isBlank()) {
            return false;
        }
        String pwdStr = new String(password);
        try {
            return verificar(pwdStr, hashAlmacenado);
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}

