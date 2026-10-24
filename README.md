# Sistema de Gestión de Funcionarios

> **Asignatura:** Desarrollo de Software Seguro
> **Tipo de proyecto:** Aplicación de escritorio (Java Swing + MySQL)
> **Repositorio:** [https://github.com/Santiago-palacio/gestion-funcionarios](https://github.com/Santiago-palacio/gestion-funcionarios)

---

## Tabla de Contenidos

1. [Descripción del Proyecto](#1-descripción-del-proyecto)
2. [Objetivo General](#2-objetivo-general)
3. [Tecnologías Utilizadas](#3-tecnologías-utilizadas)
4. [Funcionalidades](#4-funcionalidades)
5. [Arquitectura del Sistema y Patrón DAO](#5-arquitectura-del-sistema-y-patrón-dao)
6. [Estructura de Carpetas y Archivos Principales](#6-estructura-de-carpetas-y-archivos-principales)
7. [Requisitos Previos de Instalación](#7-requisitos-previos-de-instalación)
8. [Configuración de MySQL y la Base de Datos](#8-configuración-de-mysql-y-la-base-de-datos)
9. [Configuración de la Conexión (config.properties)](#9-configuración-de-la-conexión-configproperties)
10. [Compilación, Pruebas y Ejecución](#10-compilación-pruebas-y-ejecución)
11. [Validaciones y Manejo de Excepciones](#11-validaciones-y-manejo-de-excepciones)
12. [Pruebas Automatizadas](#12-pruebas-automatizadas)
13. [Seguridad y Protección de Credenciales](#13-seguridad-y-protección-de-credenciales)
14. [Integrantes del Proyecto](#14-integrantes-del-proyecto)

---

## 1. Descripción del Proyecto

El **Sistema de Gestión de Funcionarios** es una aplicación de escritorio desarrollada en **Java 17** con interfaz gráfica construida sobre **Swing**. Permite gestionar de forma centralizada la información personal, familiar y académica de los funcionarios de una organización. La aplicación interactúa con una base de datos **MySQL** a través del conector **JDBC**, siguiendo el patrón arquitectónico **DAO (Data Access Object)** para separar la lógica de presentación de la lógica de persistencia.

El proyecto fue desarrollado en el marco de la asignatura **Desarrollo de Software Seguro**, por lo que integra un conjunto de medidas técnicas orientadas a prevenir vulnerabilidades comunes: inyección SQL, filtración de credenciales, entradas maliciosas y pérdida accidental de datos.

---

## 2. Objetivo General

Diseñar e implementar una aplicación de escritorio que permita realizar las operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre el registro de funcionarios y sus entidades relacionadas (grupo familiar y estudios académicos), aplicando principios de **desarrollo de software seguro** mediante el uso de sentencias preparadas, validación de entradas por lista blanca, gestión segura de credenciales y manejo controlado de excepciones.

---

## 3. Tecnologías Utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| **Java (JDK)** | 17 (LTS) | Lenguaje principal de desarrollo |
| **Java Swing** | Incluida en JDK 17 | Interfaz gráfica de escritorio |
| **Apache Maven** | 3.8 o superior | Gestión de dependencias y compilación |
| **MySQL Server** | 8.0 / 8.4 | Motor de base de datos relacional |
| **MySQL Connector/J (JDBC)** | 9.3.0 | Conector para comunicación Java–MySQL |
| **JUnit Jupiter (JUnit 5)** | 5.11.0 | Framework de pruebas unitarias automatizadas |
| **maven-compiler-plugin** | 3.13.0 | Compilación con Java 17 y UTF-8 |
| **maven-surefire-plugin** | 3.3.0 | Ejecución de pruebas con Maven |
| **exec-maven-plugin** | 3.4.1 | Lanzamiento de la aplicación con `mvn exec:java` |
| **maven-jar-plugin** | 3.4.2 | Empaquetado como JAR ejecutable |

> Las versiones de los plugins y dependencias fueron verificadas directamente en el archivo `pom.xml` del proyecto.

---

## 4. Funcionalidades

### Gestión de Funcionarios (entidad principal)

- **Registrar** un nuevo funcionario con datos personales: tipo de documento, número de documento, nombres, apellidos, estado civil, sexo, dirección, teléfono y fecha de nacimiento.
- **Consultar** todos los funcionarios registrados en una tabla interactiva.
- **Buscar** por número de documento para cargar sus datos en el formulario.
- **Actualizar** los datos de un funcionario existente.
- **Eliminar** un funcionario con confirmación explícita mediante diálogo modal. Si el funcionario tiene familiares o estudios vinculados, el sistema impide la eliminación preservando la integridad referencial.

### Gestión de Grupo Familiar

- **Agregar** miembros del grupo familiar vinculados al funcionario seleccionado (nombres, apellidos, parentesco y fecha de nacimiento).
- **Consultar** el listado de familiares del funcionario activo en una pestaña dedicada.
- **Eliminar** un familiar registrado.

### Gestión de Estudios Académicos

- **Agregar** estudios académicos del funcionario (institución, nivel académico y título obtenido).
- **Consultar** los estudios del funcionario activo en una pestaña dedicada.
- **Eliminar** un estudio registrado.

---

## 5. Arquitectura del Sistema y Patrón DAO

La aplicación sigue una **arquitectura multicapa** que separa claramente las responsabilidades:

```
+----------------------------------+
|         Capa de Vista            |
|  VentanaFuncionarios (Swing)     |
|  ValidadorFuncionario            |
+----------------+-----------------+
                 | invoca
+----------------v-----------------+
|         Capa DAO                 |
|  Interfaces: FuncionarioDAO,     |
|  GrupoFamiliarDAO, EstudioDAO    |
|  Implementaciones: *DAOImpl      |
+----------------+-----------------+
                 | usa
+----------------v-----------------+
|        Capa de Conexion          |
|  ConexionBD (JDBC / config)      |
+----------------+-----------------+
                 | accede a
+----------------v-----------------+
|        Base de Datos             |
|  MySQL: gestion_funcionarios     |
|  Tablas: funcionarios,           |
|  grupo_familiar, estudios        |
+----------------------------------+
```

### Patrón DAO — Principios aplicados

| Principio | Descripción |
|---|---|
| **Desacoplamiento** | La capa visual (Swing) no ejecuta consultas SQL directamente. Solo invoca métodos de las interfaces DAO. |
| **Abstracción** | Las interfaces (`FuncionarioDAO`, `GrupoFamiliarDAO`, `EstudioDAO`) definen los contratos de persistencia. Las implementaciones (`*DAOImpl`) contienen el código JDBC concreto. |
| **Mantenibilidad** | Si se requiere cambiar el motor de base de datos, únicamente se reemplaza la capa de implementación sin modificar la vista. |
| **Testabilidad** | Los modelos y validadores pueden probarse en memoria sin conexión real a la base de datos. |

### Modelo de Datos — Relaciones entre Tablas

Las tres tablas de la base de datos `gestion_funcionarios` se relacionan así:

- **`funcionarios`** (entidad principal / fuerte): `id_funcionario` PK autoincremental, `numero_documento` UNIQUE, y atributos personales (tipo_documento, nombres, apellidos, estado_civil, sexo, direccion, telefono, fecha_nacimiento).
- **`grupo_familiar`** (entidad débil, relación 1:N): `id_familiar` PK, `id_funcionario` FK con `ON DELETE RESTRICT`. Atributos: nombres, apellidos, parentesco, fecha_nacimiento.
- **`estudios`** (entidad débil, relación 1:N): `id_estudio` PK, `id_funcionario` FK con `ON DELETE RESTRICT`. Atributos: universidad, nivel_academico, titulo_obtenido.

Si se intenta eliminar un funcionario con registros dependientes en `grupo_familiar` o `estudios`, MySQL rechaza la operación (error SQL 1451) y la aplicación notifica al usuario en español.

---

## 6. Estructura de Carpetas y Archivos Principales

```
gestion-funcionarios/                      <- Raiz del proyecto Maven
|
+-- pom.xml                                <- Configuracion Maven (dependencias y plugins)
+-- config.properties.example             <- Plantilla de configuracion (sin credenciales reales)
+-- config.properties                     <- EXCLUIDO por .gitignore (credenciales locales)
+-- gestion_funcionarios.sql              <- Script SQL para instalaciones NUEVAS
+-- .gitignore                            <- Excluye target/, config.properties, *.class, etc.
|
+-- src/
    +-- main/
    |   +-- java/
    |       +-- com/gestionfuncionarios/
    |           +-- App.java                   <- Punto de entrada (main), invoca Swing EDT
    |           +-- conexion/
    |           |   +-- ConexionBD.java        <- Gestor de conexion JDBC (jerarquico y seguro)
    |           +-- modelo/
    |           |   +-- Funcionario.java       <- Entidad principal con getters, equals y hashCode
    |           |   +-- GrupoFamiliar.java     <- Entidad familiar con relacion N:1
    |           |   +-- Estudio.java           <- Entidad de formacion academica con relacion N:1
    |           +-- dao/
    |           |   +-- FuncionarioDAO.java        <- Interfaz DAO (CRUD + busqueda por documento)
    |           |   +-- FuncionarioDAOImpl.java     <- Implementacion JDBC con PreparedStatement
    |           |   +-- GrupoFamiliarDAO.java       <- Interfaz DAO para grupo familiar
    |           |   +-- GrupoFamiliarDAOImpl.java   <- Implementacion JDBC
    |           |   +-- EstudioDAO.java             <- Interfaz DAO para estudios
    |           |   +-- EstudioDAOImpl.java         <- Implementacion JDBC
    |           +-- vista/
    |           |   +-- VentanaFuncionarios.java    <- Ventana principal Swing (CRUD + pestanas)
    |           |   +-- ValidadorFuncionario.java   <- Validacion de entradas con whitelist y fechas
    |           +-- app/
    |               +-- PruebaConexion.java         <- Herramienta de diagnostico de conexion JDBC
    |
    +-- test/
        +-- java/
            +-- com/gestionfuncionarios/
                +-- AppTest.java           <- 13 pruebas unitarias con JUnit 5
```

---

## 7. Requisitos Previos de Instalación

Antes de clonar y ejecutar el proyecto, asegúrese de tener instalado:

| Requisito | Versión mínima | Verificación |
|---|---|---|
| **JDK (Java Development Kit)** | 17 (LTS) | `java -version` |
| **Apache Maven** | 3.8 | `mvn -version` |
| **MySQL Server** | 8.0 o 8.4 | Servicio activo |
| **Git** | 2.x | `git --version` |

> **Sistema Operativo compatible:** Windows 10/11, Linux o macOS.
> **Editor recomendado:** Visual Studio Code con Extension Pack for Java, IntelliJ IDEA o NetBeans.

---

## 8. Configuración de MySQL y la Base de Datos

> **⚠ ADVERTENCIA — Script SQL para instalaciones nuevas únicamente:**
> El archivo `gestion_funcionarios.sql` está diseñado **exclusivamente** para inicializar una base de datos en un entorno **nuevo y vacío**.
> **NO ejecute este script sobre una base de datos existente que ya contenga las tablas del proyecto y datos reales**, ya que podría generar conflictos de claves primarias o errores de integridad referencial.
> Si su instalación ya tiene la base de datos configurada y con datos, **omita este paso completamente**.

### Pasos para instalación nueva:

**1.** Inicie el servicio de MySQL en su equipo.

**2.** Ejecute el script de creación desde la terminal. Ubíquese dentro de `gestion-funcionarios/`:

```bash
# Opcion A: Desde consola (CMD o Bash)
mysql -u root -p < gestion_funcionarios.sql

# Opcion B: Desde MySQL Workbench o DBeaver
# Abra el archivo gestion_funcionarios.sql y ejecutelo.
```

**3.** El script realizará las siguientes acciones:

- Creará la base de datos `gestion_funcionarios` (con `CREATE DATABASE IF NOT EXISTS`).
- Creará las tres tablas: `funcionarios`, `grupo_familiar` y `estudios` con sus índices y llaves foráneas.
- Insertará 3 funcionarios de prueba con sus familiares y estudios de ejemplo (usando `INSERT IGNORE` para evitar duplicados si ya existen).

---

## 9. Configuración de la Conexión (config.properties)

La conexión a MySQL se configura mediante el archivo `config.properties`, que **no se incluye en el repositorio** (excluido por `.gitignore`) para proteger las credenciales.

### Pasos:

**1.** Dentro de la carpeta `gestion-funcionarios/`, copie el archivo de plantilla:

```bash
# Windows CMD
copy config.properties.example config.properties

# Linux / macOS
cp config.properties.example config.properties
```

**2.** Abra `config.properties` con un editor de texto y complete sus credenciales locales:

```properties
db.url=jdbc:mysql://localhost:3306/gestion_funcionarios?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota
db.user=root
db.password=SU_CONTRASENA_DE_MYSQL
```

> **IMPORTANTE:** Nunca suba `config.properties` a GitHub. El archivo ya está listado en `.gitignore`. Verifique con `git status` que no aparezca rastreado antes de hacer un commit.

**3.** *(Alternativa)* Configure las credenciales mediante **variables de entorno** sin necesidad de crear el archivo:

```powershell
# Windows PowerShell
$env:DB_URL      = "jdbc:mysql://localhost:3306/gestion_funcionarios?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota"
$env:DB_USER     = "root"
$env:DB_PASSWORD = "SU_CONTRASENA_DE_MYSQL"
```

```bash
# Linux / macOS
export DB_URL="jdbc:mysql://localhost:3306/gestion_funcionarios?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota"
export DB_USER="root"
export DB_PASSWORD="SU_CONTRASENA_DE_MYSQL"
```

La clase `ConexionBD` lee las credenciales en el siguiente orden de prioridad:

1. **Variables de entorno del sistema** (`DB_URL`, `DB_USER`, `DB_PASSWORD` o `MYSQL_PWD`).
2. **Archivo `config.properties`** en el directorio actual, directorio padre o classpath.
3. **Valores por defecto** (localhost:3306, usuario root, contraseña vacía).

---

## 10. Compilación, Pruebas y Ejecución

Abra una terminal (**PowerShell** o **CMD** en Windows, o **bash** en Linux/macOS) y navegue a la carpeta raíz del proyecto Maven:

```
cd c:\...\Proyecto_Gestion_Funcionarios\gestion-funcionarios
```

### A. Limpiar y compilar el proyecto

```bash
mvn clean compile
```

Compila el código fuente de `src/main/java/` y descarga las dependencias (MySQL Connector/J 9.3.0 y JUnit 5.11.0).

---

### B. Ejecutar las pruebas unitarias automatizadas

```bash
mvn test
```

Ejecuta las **13 pruebas unitarias** de `AppTest.java` con JUnit Jupiter (JUnit 5). No requieren conexión real a MySQL.

---

### C. Verificar la conexión a la base de datos (diagnóstico)

```bash
mvn exec:java -Dexec.mainClass="com.gestionfuncionarios.app.PruebaConexion"
```

Imprime por consola el estado de la conexión JDBC y el conteo de registros en las tres tablas.

---

### D. Ejecutar la aplicación gráfica (Swing)

```bash
mvn exec:java -Dexec.mainClass="com.gestionfuncionarios.App"
```

O de forma equivalente (la clase principal ya está declarada en el `pom.xml`):

```bash
mvn exec:java
```

---

### E. Empaquetar y ejecutar como JAR autónomo

```bash
mvn clean package
java -jar target/gestion-funcionarios-1.0-SNAPSHOT.jar
```

> **Nota:** Para ejecutar el JAR, `config.properties` debe estar en el directorio de lanzamiento, o las credenciales deben estar configuradas como variables de entorno.

---

## 11. Validaciones y Manejo de Excepciones

La clase `ValidadorFuncionario` implementa validaciones previas al envío de datos a la base de datos, siguiendo los principios de **validación por lista blanca (whitelisting)**:

### Validaciones para Funcionarios

| Campo | Regla |
|---|---|
| Tipo de documento | Obligatorio, máximo 20 caracteres |
| Número de documento | Obligatorio, 3–20 caracteres, solo letras, números o guiones (`^[a-zA-Z0-9-]+$`) |
| Nombres | Obligatorio, máximo 100 caracteres |
| Apellidos | Obligatorio, máximo 100 caracteres |
| Dirección | Opcional, máximo 200 caracteres |
| Teléfono | Opcional, máx. 20 chars, solo dígitos, `+`, `-`, `()` o espacios (`^[0-9+() -]+$`) |
| Fecha de nacimiento | Opcional. Formato estricto `AAAA-MM-DD`. No puede ser futura, ni mayor a 120 años, ni contener un día inválido en el mes |

### Validaciones para Grupo Familiar y Estudios

| Entidad | Campos obligatorios | Restricciones de longitud |
|---|---|---|
| Grupo familiar | Nombres, apellidos, parentesco | Nombres y apellidos: máx. 100 chars; Parentesco: máx. 50 chars |
| Estudios | Universidad, nivel académico | Universidad y título: máx. 150 chars; Nivel académico: máx. 100 chars |

### Manejo de Excepciones SQL

| Código SQL | Causa | Acción |
|---|---|---|
| `1062` | Documento duplicado (`UNIQUE`) | Notificación al usuario en español |
| `1451` | Restricción de clave foránea al eliminar | Indicación de eliminar primero los registros dependientes |

Todos los recursos JDBC (`Connection`, `PreparedStatement`, `ResultSet`) se cierran con `try-with-resources`.

---

## 12. Pruebas Automatizadas

El archivo `src/test/java/com/gestionfuncionarios/AppTest.java` contiene **13 pruebas unitarias** con JUnit 5 que **no dependen de conexión real a la base de datos**:

| N.º | Nombre del test | Qué valida |
|---|---|---|
| 1 | `testModeloFuncionario` | El modelo `Funcionario` almacena y retorna correctamente todos sus atributos |
| 2 | `testValidadorCamposVacios` | El validador rechaza nombres, documento y apellidos vacíos |
| 3 | `testValidadorFechas` | Rechaza fechas con formato incorrecto y fechas futuras |
| 4 | `testValidadorEntradaCorrecta` | Acepta entradas válidas y parsea correctamente la fecha |
| 5 | `testSeguridadConexion` | `ConexionBD.getInfoConexion()` enmascara la contraseña como `[PROTEGIDA]` |
| 6 | `testModelosRelacionados` | `GrupoFamiliar` y `Estudio` se inicializan y representan correctamente |
| 7 | `testValidadorDocumentosInvalidos` | Rechaza documentos muy cortos, muy largos y con caracteres de inyección SQL |
| 8 | `testValidadorLongitudesMaximas` | Verifica límites máximos de nombres, apellidos, dirección y teléfono |
| 9 | `testValidadorFechasLimitesYAntiguas` | Rechaza fechas de más de 120 años, días inexistentes y 29-feb en años no bisiestos |
| 10 | `testValidadorTelefonoCaracteres` | Rechaza teléfonos con letras; acepta formatos válidos con `+`, `-`, `()` |
| 11 | `testValidadorFamiliar` | Valida obligatoriedad y fechas para registros de grupo familiar |
| 12 | `testValidadorEstudio` | Valida obligatoriedad y longitudes para registros de estudios académicos |
| 13 | `testEqualsYHashCodeModelos` | `equals` y `hashCode` se comportan coherentemente en los tres modelos |

### Ejecutar las pruebas:

```bash
mvn test
```

---

## 13. Seguridad y Protección de Credenciales

El proyecto implementa las siguientes medidas de seguridad en cumplimiento con los lineamientos de la asignatura **Desarrollo de Software Seguro**:

### 1. Prevención de Inyección SQL

- El **100% de las consultas** a la base de datos utilizan `PreparedStatement` con parámetros posicionales (`?`).
- No existe ninguna concatenación de variables del usuario en cadenas SQL.

### 2. Gestión Segura de Credenciales

- Las credenciales de MySQL **no están codificadas** en ningún archivo Java comprometido en el repositorio.
- El archivo `config.properties` está **excluido** por `.gitignore` y nunca se publica en GitHub.
- Se provee `config.properties.example` como plantilla pública sin datos reales.
- La jerarquía de configuración (variables de entorno → archivo → valores por defecto) permite despliegues seguros en entornos CI/CD sin exponer contraseñas.
- El método `ConexionBD.getInfoConexion()` enmascara siempre la contraseña como `[PROTEGIDA]` en logs y diagnósticos.

### 3. Validación de Entradas (Input Validation)

- Se aplica **lista blanca (whitelist)** mediante expresiones regulares para documentos y teléfonos.
- Las longitudes máximas se validan **antes** de enviar datos a la capa DAO, alineadas con las restricciones del esquema SQL.
- Las fechas se validan con `ResolverStyle.STRICT` para rechazar días inexistentes en el calendario.

### 4. Prevención de Pérdida de Datos

- La eliminación de funcionarios requiere **confirmación explícita** mediante diálogo modal con el nombre y documento del registro a borrar.
- La restricción `ON DELETE RESTRICT` en las tablas relacionadas impide borrados en cascada no intencionales.

### 5. Manejo Controlado de Recursos

- Todos los recursos JDBC se cierran con `try-with-resources`.
- Las conversiones de tipo (`Integer.parseInt`, `LocalDate.parse`) están protegidas contra excepciones de formato.

---

## 14. Integrantes del Proyecto

| Nombre completo | Código estudiantil | Rol |
|---|---|---|
| [Nombre del integrante 1] | [Código] | Desarrollador principal |
| [Nombre del integrante 2] | [Código] | Desarrollador / Documentación |

> *(Complete los nombres y códigos estudiantiles de los integrantes del equipo.)*

---

## Enlace al Repositorio

[https://github.com/Santiago-palacio/gestion-funcionarios](https://github.com/Santiago-palacio/gestion-funcionarios)

---

*Proyecto desarrollado para la asignatura **Desarrollo de Software Seguro** — 2024.*
