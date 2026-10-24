package com.gestionfuncionarios;

import com.gestionfuncionarios.conexion.ConexionBD;
import com.gestionfuncionarios.modelo.Estudio;
import com.gestionfuncionarios.modelo.Funcionario;
import com.gestionfuncionarios.modelo.GrupoFamiliar;
import com.gestionfuncionarios.vista.ValidadorFuncionario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para validar las reglas de negocio, modelos y validaciones seguras.
 * No dependen de conexion real a base de datos.
 */
public class AppTest {

    // ==============================================================
    // 6 PRUEBAS ORIGINALES EXISTENTES (CONSERVADAS)
    // ==============================================================

    @Test
    @DisplayName("El modelo Funcionario debe almacenar y entregar correctamente sus atributos")
    public void testModeloFuncionario() {
        Funcionario f = new Funcionario(
                1, "CC", "1000000001", "Ana", "Gomez Perez",
                "Soltera", "Femenino", "Calle 10 # 5-20", "3001112233",
                LocalDate.of(1995, 4, 12)
        );

        assertEquals(1, f.getIdFuncionario());
        assertEquals("CC", f.getTipoDocumento());
        assertEquals("1000000001", f.getNumeroDocumento());
        assertEquals("Ana Gomez Perez", f.getNombreCompleto());
        assertEquals(LocalDate.of(1995, 4, 12), f.getFechaNacimiento());
    }

    @Test
    @DisplayName("El validador debe rechazar registros con campos obligatorios vacios")
    public void testValidadorCamposVacios() {
        // Nombres vacios
        ValidadorFuncionario.ResultadoValidacion r1 = ValidadorFuncionario.validar(
                "CC", "12345678", "", "Perez", "1990-01-01", "3001234567", "Calle 1"
        );
        assertFalse(r1.isValido());
        assertTrue(r1.getMensajeError().contains("nombres"));

        // Documento vacio
        ValidadorFuncionario.ResultadoValidacion r2 = ValidadorFuncionario.validar(
                "CC", " ", "Juan", "Perez", "1990-01-01", "3001234567", "Calle 1"
        );
        assertFalse(r2.isValido());
        assertTrue(r2.getMensajeError().contains("documento"));

        // Apellidos vacios
        ValidadorFuncionario.ResultadoValidacion r3 = ValidadorFuncionario.validar(
                "CC", "12345678", "Juan", "", "1990-01-01", "3001234567", "Calle 1"
        );
        assertFalse(r3.isValido());
        assertTrue(r3.getMensajeError().contains("apellidos"));
    }

    @Test
    @DisplayName("El validador debe rechazar fechas en formato incorrecto o fechas futuras")
    public void testValidadorFechas() {
        // Formato incorrecto
        ValidadorFuncionario.ResultadoValidacion r1 = ValidadorFuncionario.validar(
                "CC", "12345678", "Juan", "Perez", "12/04/1995", "3001234567", "Calle 1"
        );
        assertFalse(r1.isValido());
        assertTrue(r1.getMensajeError().contains("AAAA-MM-DD"));

        // Fecha futura
        String fechaFutura = LocalDate.now().plusYears(1).toString();
        ValidadorFuncionario.ResultadoValidacion r2 = ValidadorFuncionario.validar(
                "CC", "12345678", "Juan", "Perez", fechaFutura, "3001234567", "Calle 1"
        );
        assertFalse(r2.isValido());
        assertTrue(r2.getMensajeError().contains("futura"));
    }

    @Test
    @DisplayName("El validador debe aceptar entradas correctas y parsear la fecha")
    public void testValidadorEntradaCorrecta() {
        ValidadorFuncionario.ResultadoValidacion r = ValidadorFuncionario.validar(
                "CC", "1020304050", "Carlos", "Rodriguez", "1988-09-25", "3119876543", "Carrera 15"
        );
        assertTrue(r.isValido());
        assertNull(r.getMensajeError());

        LocalDate fecha = ValidadorFuncionario.parsearFecha("1988-09-25");
        assertNotNull(fecha);
        assertEquals(1988, fecha.getYear());
        assertEquals(9, fecha.getMonthValue());
        assertEquals(25, fecha.getDayOfMonth());
    }

    @Test
    @DisplayName("La informacion publica de ConexionBD no debe exponer contraseñas")
    public void testSeguridadConexion() {
        String info = ConexionBD.getInfoConexion();
        assertNotNull(info);
        assertTrue(info.contains("[PROTEGIDA]"));
        assertFalse(info.contains("password="));
    }

    @Test
    @DisplayName("Los modelos GrupoFamiliar y Estudio deben inicializarse correctamente")
    public void testModelosRelacionados() {
        GrupoFamiliar familiar = new GrupoFamiliar(1, 10, "Laura", "Gomez", "Hermana", LocalDate.of(2000, 6, 15));
        assertEquals("Laura Gomez (Hermana)", familiar.toString());

        Estudio estudio = new Estudio(1, 10, "Universidad Nacional", "Universitario", "Ingenieria");
        assertEquals("Ingenieria (Universidad Nacional - Universitario)", estudio.toString());
    }

    // ==============================================================
    // NUEVAS PRUEBAS DE SEGURIDAD, LIMITES Y VALIDACIONES
    // ==============================================================

    @Test
    @DisplayName("El validador debe rechazar documentos con longitud o caracteres invalidos")
    public void testValidadorDocumentosInvalidos() {
        // Documento muy corto (< 3 caracteres)
        ValidadorFuncionario.ResultadoValidacion rCorto = ValidadorFuncionario.validar(
                "CC", "12", "Ana", "Ruiz", "1995-05-10", "3001234567", "Calle 1"
        );
        assertFalse(rCorto.isValido());
        assertTrue(rCorto.getMensajeError().contains("entre 3 y 20"));

        // Documento muy largo (> 20 caracteres)
        ValidadorFuncionario.ResultadoValidacion rLargo = ValidadorFuncionario.validar(
                "CC", "123456789012345678901", "Ana", "Ruiz", "1995-05-10", "3001234567", "Calle 1"
        );
        assertFalse(rLargo.isValido());
        assertTrue(rLargo.getMensajeError().contains("entre 3 y 20"));

        // Documento con caracteres maliciosos o no alfanumericos
        ValidadorFuncionario.ResultadoValidacion rInyeccion = ValidadorFuncionario.validar(
                "CC", "1000' OR '1'='1", "Ana", "Ruiz", "1995-05-10", "3001234567", "Calle 1"
        );
        assertFalse(rInyeccion.isValido());
        assertTrue(rInyeccion.getMensajeError().contains("letras, numeros o guiones"));

        ValidadorFuncionario.ResultadoValidacion rEspacios = ValidadorFuncionario.validar(
                "CC", "1000 2000", "Ana", "Ruiz", "1995-05-10", "3001234567", "Calle 1"
        );
        assertFalse(rEspacios.isValido());
        assertTrue(rEspacios.getMensajeError().contains("letras, numeros o guiones"));
    }

    @Test
    @DisplayName("El validador debe verificar longitudes maximas de campos de texto")
    public void testValidadorLongitudesMaximas() {
        String texto101 = "A".repeat(101);
        String texto201 = "B".repeat(201);
        String texto21 = "1".repeat(21);

        // Nombres > 100
        ValidadorFuncionario.ResultadoValidacion rNom = ValidadorFuncionario.validar(
                "CC", "10203040", texto101, "Perez", "1990-01-01", "3001234567", "Calle 1"
        );
        assertFalse(rNom.isValido());
        assertTrue(rNom.getMensajeError().contains("nombres no pueden exceder"));

        // Apellidos > 100
        ValidadorFuncionario.ResultadoValidacion rApe = ValidadorFuncionario.validar(
                "CC", "10203040", "Carlos", texto101, "1990-01-01", "3001234567", "Calle 1"
        );
        assertFalse(rApe.isValido());
        assertTrue(rApe.getMensajeError().contains("apellidos no pueden exceder"));

        // Direccion > 200
        ValidadorFuncionario.ResultadoValidacion rDir = ValidadorFuncionario.validar(
                "CC", "10203040", "Carlos", "Perez", "1990-01-01", "3001234567", texto201
        );
        assertFalse(rDir.isValido());
        assertTrue(rDir.getMensajeError().contains("direccion no puede exceder"));

        // Telefono > 20
        ValidadorFuncionario.ResultadoValidacion rTel = ValidadorFuncionario.validar(
                "CC", "10203040", "Carlos", "Perez", "1990-01-01", texto21, "Calle 1"
        );
        assertFalse(rTel.isValido());
        assertTrue(rTel.getMensajeError().contains("telefono no puede exceder"));
    }

    @Test
    @DisplayName("El validador debe rechazar fechas demasiado antiguas o dias invalidos en calendario")
    public void testValidadorFechasLimitesYAntiguas() {
        // Fecha de hace mas de 120 anos (inconsistencia biologica)
        String fechaAntigua = LocalDate.now().minusYears(125).toString();
        ValidadorFuncionario.ResultadoValidacion rAntigua = ValidadorFuncionario.validar(
                "CC", "10203040", "Carlos", "Perez", fechaAntigua, "3001234567", "Calle 1"
        );
        assertFalse(rAntigua.isValido());
        assertTrue(rAntigua.getMensajeError().contains("120 anos"));

        // Dia inexistente en mes de 30 dias (31 de abril)
        ValidadorFuncionario.ResultadoValidacion rDiaInexistente = ValidadorFuncionario.validar(
                "CC", "10203040", "Carlos", "Perez", "1990-04-31", "3001234567", "Calle 1"
        );
        assertFalse(rDiaInexistente.isValido());

        // 29 de febrero en anio no bisiesto
        ValidadorFuncionario.ResultadoValidacion rBisiestoInvalido = ValidadorFuncionario.validar(
                "CC", "10203040", "Carlos", "Perez", "2021-02-29", "3001234567", "Calle 1"
        );
        assertFalse(rBisiestoInvalido.isValido());
    }

    @Test
    @DisplayName("El validador debe verificar caracteres permitidos en telefono")
    public void testValidadorTelefonoCaracteres() {
        // Telefono con letras
        ValidadorFuncionario.ResultadoValidacion rLetras = ValidadorFuncionario.validar(
                "CC", "10203040", "Carlos", "Perez", "1990-01-01", "300TEL1234", "Calle 1"
        );
        assertFalse(rLetras.isValido());
        assertTrue(rLetras.getMensajeError().contains("caracteres no validos"));

        // Telefonos con formatos validos (+, -, espacios, parentesis)
        ValidadorFuncionario.ResultadoValidacion rValido1 = ValidadorFuncionario.validar(
                "CC", "10203040", "Carlos", "Perez", "1990-01-01", "+57 300 123-4567", "Calle 1"
        );
        assertTrue(rValido1.isValido());

        ValidadorFuncionario.ResultadoValidacion rValido2 = ValidadorFuncionario.validar(
                "CC", "10203040", "Carlos", "Perez", "1990-01-01", "(601) 2345678", "Calle 1"
        );
        assertTrue(rValido2.isValido());
    }

    @Test
    @DisplayName("El validador debe controlar datos obligatorios y fechas para familiares")
    public void testValidadorFamiliar() {
        // Nombres de familiar vacio
        ValidadorFuncionario.ResultadoValidacion rNom = 
                ValidadorFuncionario.validarFamiliar("", "Perez", "Hijo(a)", "2015-05-10");
        assertFalse(rNom.isValido());
        assertTrue(rNom.getMensajeError().contains("nombres"));

        // Apellidos de familiar vacio
        ValidadorFuncionario.ResultadoValidacion rApe = 
                ValidadorFuncionario.validarFamiliar("Pedro", "", "Hijo(a)", "2015-05-10");
        assertFalse(rApe.isValido());
        assertTrue(rApe.getMensajeError().contains("apellidos"));

        // Parentesco vacio
        ValidadorFuncionario.ResultadoValidacion rPar = 
                ValidadorFuncionario.validarFamiliar("Pedro", "Perez", "", "2015-05-10");
        assertFalse(rPar.isValido());
        assertTrue(rPar.getMensajeError().contains("parentesco"));

        // Fecha de familiar futura
        String fechaFutura = LocalDate.now().plusDays(5).toString();
        ValidadorFuncionario.ResultadoValidacion rFecha = 
                ValidadorFuncionario.validarFamiliar("Pedro", "Perez", "Hijo(a)", fechaFutura);
        assertFalse(rFecha.isValido());
        assertTrue(rFecha.getMensajeError().contains("futura"));

        // Familiar correcto
        ValidadorFuncionario.ResultadoValidacion rOk = 
                ValidadorFuncionario.validarFamiliar("Pedro", "Perez", "Hijo(a)", "2015-05-10");
        assertTrue(rOk.isValido());
    }

    @Test
    @DisplayName("El validador debe controlar datos obligatorios y longitudes para estudios")
    public void testValidadorEstudio() {
        // Universidad vacia
        ValidadorFuncionario.ResultadoValidacion rUniv = 
                ValidadorFuncionario.validarEstudio("", "Universitario", "Ingenieria");
        assertFalse(rUniv.isValido());
        assertTrue(rUniv.getMensajeError().contains("universidad"));

        // Nivel academico vacio
        ValidadorFuncionario.ResultadoValidacion rNiv = 
                ValidadorFuncionario.validarEstudio("Universidad Nacional", "", "Ingenieria");
        assertFalse(rNiv.isValido());
        assertTrue(rNiv.getMensajeError().contains("nivel"));

        // Longitud excesiva de institucion (> 150)
        String univLarga = "U".repeat(151);
        ValidadorFuncionario.ResultadoValidacion rLarga = 
                ValidadorFuncionario.validarEstudio(univLarga, "Universitario", "Ingenieria");
        assertFalse(rLarga.isValido());
        assertTrue(rLarga.getMensajeError().contains("150"));

        // Estudio valido
        ValidadorFuncionario.ResultadoValidacion rOk = 
                ValidadorFuncionario.validarEstudio("Universidad Nacional", "Universitario", "Ingenieria");
        assertTrue(rOk.isValido());
    }

    @Test
    @DisplayName("Los metodos equals y hashCode deben comportarse coherentemente")
    public void testEqualsYHashCodeModelos() {
        Funcionario f1 = new Funcionario(1, "CC", "1001", "A", "B", "Soltero", "M", "Dir", "123", LocalDate.now());
        Funcionario f2 = new Funcionario(1, "CC", "1001", "A", "B", "Soltero", "M", "Dir", "123", LocalDate.now());
        Funcionario f3 = new Funcionario(2, "CC", "1002", "C", "D", "Casado", "M", "Dir2", "456", LocalDate.now());

        assertEquals(f1, f2);
        assertNotEquals(f1, f3);
        assertEquals(f1.hashCode(), f2.hashCode());

        GrupoFamiliar gf1 = new GrupoFamiliar(1, 1, "N", "A", "Hijo", LocalDate.now());
        GrupoFamiliar gf2 = new GrupoFamiliar(1, 1, "N", "A", "Hijo", LocalDate.now());
        assertEquals(gf1, gf2);
        assertEquals(gf1.hashCode(), gf2.hashCode());

        Estudio e1 = new Estudio(1, 1, "Univ", "Pregrado", "Titulo");
        Estudio e2 = new Estudio(1, 1, "Univ", "Pregrado", "Titulo");
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}
