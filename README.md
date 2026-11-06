# Sistema de Gestión de Funcionarios

> **Asignatura:** Desarrollo de Software Seguro  
> **Tipo de proyecto:** Aplicación de escritorio (Java Swing + MySQL)  
> **Repositorio:** [https://github.com/Santiago-palacio/gestion-funcionarios](https://github.com/Santiago-palacio/gestion-funcionarios)  
> **Año de desarrollo:** 2026  

---

## Tabla de Contenidos

1. [Descripción del Proyecto](#1-descripción-del-proyecto)
2. [Tecnologías Utilizadas](#2-tecnologías-utilizadas)
3. [Funcionalidades del Sistema](#3-funcionalidades-del-sistema)
4. [Autenticación y Seguridad](#4-autenticación-y-seguridad)
5. [Autorización y Matriz de Permisos por Rol](#5-autorización-y-matriz-de-permisos-por-rol)
6. [Módulo de Gestión de Usuarios](#6-módulo-de-gestión-de-usuarios)
7. [Arquitectura del Sistema y Estructura de Clases](#7-arquitectura-del-sistema-y-estructura-de-clases)
8. [Modelo de Base de Datos y Migración](#8-modelo-de-base-de-datos-y-migración)
9. [Requisitos Previos de Instalación](#9-requisitos-previos-de-instalación)
10. [Cómo Abrir y Ejecutar el Proyecto en Otro Computador (Guía del Evaluador)](#10-cómo-abrir-y-ejecutar-el-proyecto-en-otro-computador-guía-del-evaluador)
11. [Configuración de Conexión (config.properties)](#11-configuración-de-conexión-configproperties)
12. [Administración de Usuarios y Creación de Cuentas](#12-administración-de-usuarios-y-creación-de-cuentas)
13. [Compilación, Pruebas y Empaquetado](#13-compilación-pruebas-y-empaquetado)
14. [Pruebas Automatizadas (32 Pruebas Aprobadas)](#14-pruebas-automatizadas-32-pruebas-aprobadas)
15. [Medidas Técnicas de Software Seguro](#15-medidas-técnicas-de-software-seguro)
16. [Comandos de Control de Versiones (Git)](#16-comandos-de-control-de-versiones-git)
17. [Integrantes del Proyecto](#17-integrantes-del-proyecto)

---

## 1. Descripción del Proyecto

El **Sistema de Gestión de Funcionarios** es una aplicación de escritorio desarrollada en **Java 17** con interfaz gráfica construida en **Java Swing** y base de datos relacional **MySQL 8.4** mediante **JDBC**. La arquitectura del software sigue rigurosamente el patrón **DAO (Data Access Object)** para garantizar el desacoplamiento entre la presentación y la persistencia de datos.

En el marco de la asignatura **Desarrollo de Software Seguro**, la aplicación implementa:
- Autenticación segura mediante contraseñas protegidas con el algoritmo criptográfico **BCrypt** (sal aleatoria y factor de coste computacional 12).
- Control de acceso basado en roles (**RBAC - Role-Based Access Control**), distinguiendo entre roles `ADMINISTRADOR` y `CONSULTA`.
- **Defensa en profundidad en dos niveles:** la seguridad se valida tanto en la interfaz gráfica como de forma irrevocable en la **lógica de negocio**.
- Módulo administrativo exclusivo para la gestión de cuentas de usuario con protección contra el bloqueo del último administrador activo.
- Suite de **32 pruebas unitarias automatizadas** con JUnit 5 ejecutadas con `mvn clean test` (0 fallos y 0 errores).

---

## 2. Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|---|---|---|
| **Java (JDK)** | 17 (LTS) | Lenguaje de desarrollo principal |
| **Java Swing** | Incluida en JDK 17 | Interfaz gráfica de escritorio |
| **Apache Maven** | 3.8+ | Gestión de dependencias, ciclo de vida y compilación |
| **MySQL Server** | 8.0 / 8.4 | Motor de base de datos relacional |
| **MySQL Connector/J (JDBC)** | 9.3.0 | Conector oficial para comunicación Java–MySQL |
| **jBCrypt** | 0.4 | Hashing seguro de contraseñas con sal automática |
| **JUnit Jupiter (JUnit 5)** | 5.11.0 | Framework de pruebas automatizadas |

---

## 3. Funcionalidades del Sistema

### Gestión de Funcionarios (Entidad Principal)
- **Registrar:** Alta de funcionario con documento, nombres, apellidos, estado civil, sexo, dirección, teléfono y fecha de nacimiento (con validaciones estrictas y whitelisting).
- **Consultar y Buscar:** Listado general en tabla interactiva y búsqueda instantánea por número de documento de identidad.
- **Actualizar:** Modificación de datos personales respetando unicidad de documentos.
- **Eliminar:** Borrado con confirmación modal y protección de integridad referencial (`ON DELETE RESTRICT`).

### Grupo Familiar y Estudios Académicos (Relaciones 1:N)
- **Familiares:** Registro, visualización y eliminación de parientes asociados.
- **Estudios:** Registro, visualización y eliminación de formación académica institucional.

### Autenticación y Sesión
- Pantalla de inicio de sesión (`VentanaLogin`) previa al acceso al sistema.
- Cierre de sesión seguro con destrucción de referencias en memoria y retorno a la ventana de login.

### Módulo de Gestión de Usuarios (Exclusivo ADMINISTRADOR)
- Acceso desde el botón institucional "Gestión de Usuarios".
- Creación de nuevos usuarios con rol y contraseña cifrada con BCrypt.
- Activación y desactivación de cuentas en tiempo real.
- Restablecimiento seguro de contraseñas con doble confirmación.
- **Protección contra desabastecimiento:** Prohibido desactivar o quitar permisos al último administrador activo del sistema.

---

## 4. Autenticación y Seguridad

### Flujo de Verificación Criptográfica

```
Usuario ejecuta la aplicación
        │
        ▼
VentanaLogin (Swing)
  - Solicita: Usuario y Contraseña (JPasswordField)
  - Valida campos obligatorios
        │
        ▼
ServicioAutenticacion.autenticar(username, char[])
  1. PreparedStatement: SELECT ... FROM usuarios WHERE username = ?
  2. Si usuario no existe → Retorna "Usuario o contraseña incorrectos" (Mensaje neutral)
  3. Si cuenta inactiva (activo = 0) → Retorna "La cuenta de usuario se encuentra inactiva"
  4. BCrypt.checkpw(passwordIngresada, hashAlmacenado)
  5. Si no coincide → Retorna "Usuario o contraseña incorrectos"
  6. Si coincide → SesionUsuario.getInstancia().iniciarSesion(usuario)
  7. Destrucción del arreglo char[] con Arrays.fill(pwd, '\0')
        │
        ▼
VentanaFuncionarios
  - Despliega badge en encabezado: Usuario y Rol activo
  - Aplica permisos visuales (aplicarPermisosRol)
  - Habilita botón "Cerrar Sesión" y "Gestión de Usuarios" (solo si es ADMIN)
```

---

## 5. Autorización y Matriz de Permisos por Rol

### Matriz de Control de Acceso (RBAC)

| Operación del Sistema | Rol ADMINISTRADOR | Rol CONSULTA |
|---|:---:|:---:|
| Iniciar sesión y consultar listado general de funcionarios | ✅ Permitido | ✅ Permitido |
| Buscar funcionario por número de documento | ✅ Permitido | ✅ Permitido |
| Visualizar grupo familiar asociado | ✅ Permitido | ✅ Permitido |
| Visualizar estudios académicos asociados | ✅ Permitido | ✅ Permitido |
| **Registrar nuevo funcionario** | ✅ Permitido | ❌ Denegado |
| **Actualizar datos de funcionario** | ✅ Permitido | ❌ Denegado |
| **Eliminar funcionario** | ✅ Permitido | ❌ Denegado |
| **Agregar / Eliminar familiar** | ✅ Permitido | ❌ Denegado |
| **Agregar / Eliminar estudio académico** | ✅ Permitido | ❌ Denegado |
| **Abrir ventana Gestión de Usuarios** | ✅ Permitido | ❌ Denegado (Oculto y bloqueado) |
| **Crear cuentas / Cambiar roles / Activar / Desactivar** | ✅ Permitido | ❌ Denegado |
| **Restablecer contraseñas de usuarios** | ✅ Permitido | ❌ Denegado |

### Estrategia de Defensa en Profundidad
1. **Nivel 1 (Capa Visual):** La interfaz desactiva u oculta los controles para el rol `CONSULTA`.
2. **Nivel 2 (Lógica de Negocio):** Cada método crítico en `VentanaFuncionarios` y `ServicioUsuario` invoca `ControlAcceso.validarPermisoEscritura()`. Si no hay sesión o el rol es `CONSULTA`, se lanza inmediatamente `AccesoDenegadoException` impidiendo cualquier operación hacia los DAOs o la base de datos.

---

## 6. Módulo de Gestión de Usuarios

Ubicado en el paquete `com.gestionfuncionarios.vista.VentanaGestionUsuarios`, es una ventana modal de administración avanzada exclusiva para `ADMINISTRADOR`:

- **Tabla en vivo:** Muestra identificador, nombre de usuario, rol, estado (Activo en verde, Inactivo en rojo) y fecha de creación.
- **Formulario de edición:** Permite modificar username, rol asignado y estado.
- **Restablecimiento de contraseñas:** Diálogo seguro que solicita nueva contraseña y confirmación, cifrando el valor resultante con BCrypt.
- **Regla crítica de negocio:** Si existe un único administrador activo, el sistema impide desactivarlo o cambiarle el rol a CONSULTA, informando de la restricción al usuario.

---

## 7. Arquitectura del Sistema y Estructura de Clases

```text
com.gestionfuncionarios
├── app/
│   ├── App.java                      # Punto de entrada principal (inicia con VentanaLogin en Swing EDT)
│   ├── CrearPrimerAdmin.java         # Herramienta interactiva por consola para registrar administradores
│   └── PruebaConexion.java           # Herramienta de diagnóstico de conexión JDBC
├── conexion/
│   └── ConexionBD.java               # Conexión JDBC segura con jerarquía (Env Vars -> Archivo -> Defaults)
├── modelo/
│   ├── Funcionario.java              # Entidad principal de funcionarios
│   ├── GrupoFamiliar.java            # Entidad dependiente de parientes
│   ├── Estudio.java                  # Entidad dependiente de estudios
│   ├── Rol.java                      # Enum tipado: ADMINISTRADOR(1) y CONSULTA(2)
│   └── Usuario.java                  # Entidad de usuario con contraseña protegida en toString()
├── dao/
│   ├── FuncionarioDAO.java / Impl    # Persistencia de funcionarios
│   ├── GrupoFamiliarDAO.java / Impl  # Persistencia de grupo familiar
│   ├── EstudioDAO.java / Impl        # Persistencia de estudios académicos
│   └── UsuarioDAO.java / Impl        # Persistencia de usuarios con PreparedStatement y JOIN con roles
├── seguridad/
│   ├── PasswordHasher.java           # BCrypt (coste 12, sal aleatoria, borrado de memoria char[])
│   ├── ServicioAutenticacion.java    # Login, control de activos/inactivos, mensajes neutrales
│   ├── ServicioUsuario.java          # Lógica de negocio de cuentas y protección del último admin
│   ├── SesionUsuario.java            # Singleton gestor de sesión activa en memoria
│   ├── ControlAcceso.java            # Validación de permisos y autorización RBAC
│   └── AccesoDenegadoException.java  # Excepción no verificada ante operaciones no autorizadas
└── vista/
    ├── VentanaLogin.java             # Ventana gráfica de autenticación (Login)
    ├── VentanaFuncionarios.java      # Ventana principal del sistema (CRUD y visualización)
    ├── VentanaGestionUsuarios.java   # Módulo modal de administración de usuarios (solo ADMIN)
    └── ValidadorFuncionario.java     # Validaciones con listas blancas (whitelisting) y fechas estrictas
```

---

## 8. Modelo de Base de Datos y Migración

La base de datos utilizada es **`gestion_funcionarios`**. Se compone de 5 tablas relacionadas:

1. **`funcionarios`:** Entidad principal (`id_funcionario PK`, `numero_documento UNIQUE`).
2. **`grupo_familiar`:** Relación 1:N con `funcionarios` (`ON DELETE RESTRICT`).
3. **`estudios`:** Relación 1:N con `funcionarios` (`ON DELETE RESTRICT`).
4. **`roles`:** Catálogo de seguridad (`id_rol PK`, `nombre UNIQUE`). Contiene `ADMINISTRADOR` (1) y `CONSULTA` (2).
5. **`usuarios`:** Cuentas del sistema (`id_usuario PK`, `username UNIQUE`, `password_hash VARCHAR(255)`, `id_rol FK`, `activo TINYINT(1)`).

---

## 9. Requisitos Previos de Instalación

| Requisito | Versión recomendada | Comprobación en terminal |
|---|---|---|
| **Java JDK** | 17 (LTS) | `java -version` |
| **Apache Maven** | 3.8 o superior | `mvn -version` |
| **MySQL Server** | 8.0 o 8.4 (Servicio activo) | `mysql --version` |
| **Git** | 2.x | `git --version` |

---

## 10. Cómo Abrir y Ejecutar el Proyecto en Otro Computador (Guía del Evaluador)

Esta guía paso a paso permite al docente o evaluador descargar, configurar y ejecutar el proyecto de forma autónoma en cualquier computador con Windows, Linux o macOS:

### Paso 1: Clonar el repositorio desde GitHub
Abra una terminal y descargue el proyecto:
```bash
git clone https://github.com/Santiago-palacio/gestion-funcionarios.git
cd gestion-funcionarios
```

### Paso 2: Abrir en el editor o IDE de su preferencia
- **Visual Studio Code:** Abra la carpeta `gestion-funcionarios` (la que contiene el archivo `pom.xml`) y asegúrese de tener instalada la extensión *Extension Pack for Java*.
- **IntelliJ IDEA / NetBeans / Eclipse:** Seleccione *Open* o *Import Project* y elija el archivo `pom.xml` de `gestion-funcionarios`.

### Paso 3: Configurar la base de datos en MySQL
Inicie su servidor MySQL local y ejecute los dos scripts SQL en el siguiente orden (desde **MySQL Workbench**, **DBeaver** o consola MySQL):

1. **Script Base:** Ejecute el archivo `gestion_funcionarios.sql`:
   - Crea la base de datos `gestion_funcionarios`.
   - Crea las tablas `funcionarios`, `grupo_familiar` y `estudios` con datos de prueba.
2. **Script de Migración de Seguridad:** Ejecute el archivo `migracion_seguridad.sql`:
   - Crea las tablas `roles` y `usuarios` (migración 100% no destructiva).
   - Inserta automáticamente los roles y las **cuentas de demostración con hashes válidos de BCrypt**.

### Paso 4: Configurar credenciales de conexión
Dentro de la carpeta `gestion-funcionarios`:
1. Copie la plantilla `config.properties.example` a `config.properties`:
   ```bash
   # En Windows (CMD o PowerShell)
   copy config.properties.example config.properties

   # En Linux o macOS
   cp config.properties.example config.properties
   ```
2. Abra `config.properties` y ajuste su contraseña de MySQL local:
   ```properties
   db.url=jdbc:mysql://localhost:3306/gestion_funcionarios?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota
   db.user=root
   db.password=SU_CONTRASENA_DE_MYSQL
   ```
   *(Alternativa sin archivo: puede configurar la variable de entorno `$env:DB_PASSWORD="su_clave"` en PowerShell o `export DB_PASSWORD="su_clave"` en Linux/Mac).*

### Paso 5: Compilar y ejecutar las pruebas automatizadas
En la terminal, dentro de `gestion-funcionarios`:
```bash
mvn clean test
```
Verifique que se ejecuten y aprueben las **32 pruebas automatizadas** sin fallos (`BUILD SUCCESS`).

### Paso 6: Iniciar la aplicación gráfica
Ejecute el siguiente comando Maven:
```bash
mvn exec:java
```
Se desplegará de inmediato la ventana institucional de **Inicio de Sesión**.

### Paso 7: Iniciar sesión con las cuentas de demostración

| Rol | Usuario | Contraseña | Permisos y Comportamiento Esperado |
|---|---|---|---|
| **ADMINISTRADOR** | `admin` | `Admin123*` | **Acceso Total:** Encabezado con badge verde `ADMINISTRADOR`. Botón **Guardar** habilitado. Botones **Actualizar**, **Eliminar**, **Familiares** y **Estudios** habilitados al seleccionar registros. Acceso visible al botón azul **Gestión de Usuarios**. |
| **CONSULTA** | `consulta` | `Consulta123*` | **Solo Lectura:** Encabezado con badge ámbar `CONSULTA`. Botones de registro, actualización y borrado deshabilitados. Botón de Gestión de Usuarios oculto y bloqueado por lógica de negocio. Barra de estado inferior indica `Acceso: SOLO LECTURA`. |

Para alternar entre roles, presione el botón rojo **"Cerrar Sesión"** en la esquina superior derecha del encabezado y autentíquese con la otra cuenta.

---

## 11. Configuración de Conexión (config.properties)

La aplicación implementa una lectura jerárquica y segura de credenciales en `ConexionBD.java`:
1. **Variables de entorno del sistema:** `DB_URL`, `DB_USER`, `DB_PASSWORD` (o `MYSQL_PWD`).
2. **Archivo local `config.properties`:** Excluido del repositorio por `.gitignore` para proteger secretos.
3. **Valores por defecto:** `localhost:3306`, usuario `root`, contraseña vacía.

El método `ConexionBD.getInfoConexion()` enmascara en todo momento la contraseña como `[PROTEGIDA]` para impedir fugas en consolas y registros de diagnóstico.

---

## 12. Administración de Usuarios y Creación de Cuentas

### Cuentas de demostración ya incluidas
Al ejecutar `migracion_seguridad.sql`, quedan disponibles inmediatamente las cuentas:
- `admin` con clave `Admin123*`
- `consulta` con clave `Consulta123*`

### Creación interactiva por consola (Alternativa sin contraseñas en scripts)
Para registrar un administrador personalizado sin escribir contraseñas en texto plano:
```bash
mvn compile exec:java -Dexec.mainClass="com.gestionfuncionarios.app.CrearPrimerAdmin"
```
El asistente solicita el nombre de usuario, captura la contraseña de forma oculta en consola, calcula el hash con BCrypt (coste 12 y sal aleatoria) e inserta el registro en MySQL mediante `PreparedStatement`.

### Administración gráfica desde la aplicación
Una vez autenticado como `admin`, use el botón **"Gestión de Usuarios"** para dar de alta nuevos usuarios, alternar estados o cambiar contraseñas directamente en la interfaz.

---

## 13. Compilación, Pruebas y Empaquetado

Todos los comandos deben ejecutarse dentro de la carpeta `gestion-funcionarios`:

| Comando | Acción |
|---|---|
| `mvn clean compile` | Descarga dependencias y compila los 27 archivos fuente en Java 17 |
| `mvn clean test` | Ejecuta las **32 pruebas automatizadas** con JUnit 5 |
| `mvn exec:java` | Lanza la aplicación iniciando en la ventana de Login |
| `mvn clean package` | Genera el archivo empaquetado autónomo en `target/gestion-funcionarios-1.0-SNAPSHOT.jar` |
| `java -jar target/gestion-funcionarios-1.0-SNAPSHOT.jar` | Ejecuta el artefacto JAR empaquetado |

---

## 14. Pruebas Automatizadas (32 Pruebas Aprobadas)

El proyecto cuenta con **32 pruebas unitarias automatizadas** que no dependen de conexión activa a MySQL (usan modelos en memoria y DAOs desacoplados).

```bash
mvn clean test
```

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.gestionfuncionarios.AppTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0 -- in com.gestionfuncionarios.AppTest
[INFO] Running com.gestionfuncionarios.SeguridadTest
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0 -- in com.gestionfuncionarios.SeguridadTest
[INFO] 
[INFO] Results:
[INFO] Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Detalle de las 32 Pruebas:

#### A. Pruebas de Funcionarios, Validaciones y DAO (`AppTest.java` — 13 pruebas)
1. `testModeloFuncionario`: Almacenamiento y retorno coherente de atributos del funcionario.
2. `testValidadorCamposVacios`: Rechazo de nombres, apellidos y documento vacíos.
3. `testValidadorFechas`: Rechazo de formato no estricto y fechas futuras.
4. `testValidadorEntradaCorrecta`: Aceptación de entradas válidas y parseo cronológico.
5. `testSeguridadConexion`: Enmascaramiento de contraseñas como `[PROTEGIDA]` en `ConexionBD`.
6. `testModelosRelacionados`: Representación textual de `GrupoFamiliar` y `Estudio`.
7. `testValidadorDocumentosInvalidos`: Rechazo de documentos fuera de rango (3-20) y caracteres de SQL Injection.
8. `testValidadorLongitudesMaximas`: Respeto de límites máximos según el esquema de la base de datos.
9. `testValidadorFechasLimitesYAntiguas`: Rechazo de fechas mayores a 120 años y días inexistentes en calendario.
10. `testValidadorTelefonoCaracteres`: Whitelist para teléfonos (dígitos, espacios, guiones y paréntesis).
11. `testValidadorFamiliar`: Obligatoriedad y formato en parientes del funcionario.
12. `testValidadorEstudio`: Obligatoriedad y longitudes en formación académica.
13. `testEqualsYHashCodeModelos`: Consistencia de identidad en entidades.

#### B. Pruebas de Autenticación, Autorización y Usuarios (`SeguridadTest.java` — 19 pruebas)
14. `testPasswordHasherSalUnica`: Generación de hashes BCrypt distintos con sal única para la misma contraseña.
15. `testPasswordHasherRechazo`: Rechazo de contraseñas incorrectas, nulas, vacías o hashes corruptos.
16. `testAutenticacionAdminExitosa`: Inicio de sesión exitoso y registro en `SesionUsuario` con rol ADMINISTRADOR.
17. `testAutenticacionConsultaExitosa`: Inicio de sesión exitoso con rol CONSULTA.
18. `testAutenticacionContrasenaIncorrecta`: Emisión de mensaje neutral y bloqueo de sesión ante contraseña errónea.
19. `testAutenticacionUsuarioInexistente`: Emisión de mensaje neutral ante usuario no registrado.
20. `testAutenticacionUsuarioInactivo`: Rechazo de autenticación para cuentas inactivas (`activo = 0`).
21. `testAutenticacionCamposVacios`: Rechazo de solicitudes de login con credenciales en blanco.
22. `testCierreDeSesion`: Limpieza total de estado y nulificación de la sesión activa en memoria.
23. `testPermisosAdministrador`: Permiso concedido a todas las operaciones de escritura sin excepciones.
24. `testBloqueoEscrituraRolConsulta`: Lanzamiento de `AccesoDenegadoException` para rol CONSULTA al intentar registrar, actualizar o eliminar funcionarios, familiares o estudios.
25. `testOperacionesSinSesion`: Rechazo con `AccesoDenegadoException` ante intentos de escritura sin sesión previa.
26. `testProteccionContrasenaEnToString`: Enmascaramiento estricto de contraseñas en `Usuario.toString()`.
27. `testGestionUsuariosCrearUsuarioPorAdmin`: Creación de usuarios con contraseña hasheada en BCrypt por ADMINISTRADOR.
28. `testGestionUsuariosRechazoDuplicadosYCamposInvalidos`: Validación de unicidad de username y longitud de clave.
29. `testGestionUsuariosBloqueoRolConsulta`: Bloqueo total de operaciones del módulo de usuarios para rol CONSULTA.
30. `testGestionUsuariosProteccionUltimoAdministrador`: Prohibición de desactivar o degradar al último administrador activo.
31. `testGestionUsuariosActivarDesactivar`: Cambio controlado del estado de cuentas de usuario.
32. `testGestionUsuariosRestablecerPassword`: Restablecimiento de contraseña con verificación criptográfica en BCrypt.

---

## 15. Medidas Técnicas de Software Seguro

| Vulnerabilidad Mitigada | Medida Técnica Implementada |
|---|---|
| **Almacenamiento inseguro de credenciales** | Algoritmo **BCrypt** con factor de coste 12 y sal aleatoria única por contraseña. |
| **Inyección SQL (SQL Injection)** | **100% de consultas preparadas** con `PreparedStatement` y parámetros posicionales (`?`). |
| **Enumeración de usuarios** | Respuestas de autenticación idénticas y neutrales: `"Usuario o contraseña incorrectos"`. |
| **Inspección de memoria (Heap Inspection)** | Uso de `JPasswordField.getPassword()` (`char[]`) con borrado mediante `Arrays.fill(pwd, '\0')`. |
| **Bypass de autorización por UI** | Control de permisos en dos capas: validación en Swing y validación en la lógica de negocio (`ControlAcceso`). |
| **Denegación de servicio administrativa** | Bloqueo en lógica de negocio para impedir la desactivación del último administrador activo. |
| **Filtración de secretos en repositorio** | `config.properties` excluido por `.gitignore`. Plantilla pública `config.properties.example` sin credenciales. |
| **Fugas de contraseñas en logs** | `ConexionBD.getInfoConexion()` y `Usuario.toString()` enmascaran contraseñas como `[PROTEGIDO]`. |
| **Entradas maliciosas (Input Validation)** | Whitelisting con expresiones regulares y validación de longitudes alineadas con la base de datos. |
| **Destrucción accidental de datos** | Integridad referencial `ON DELETE RESTRICT` y confirmación modal en eliminaciones. |

---

## 16. Comandos de Control de Versiones (Git)

Para gestionar y subir los cambios al repositorio remoto en GitHub desde la raíz del proyecto:

```bash
# 1. Ubicarse en la carpeta del repositorio
cd gestion-funcionarios

# 2. Verificar que config.properties permanezca ignorado y no rastreado
git status

# 3. Preparar archivos para commit
git add pom.xml
git add README.md
git add migracion_seguridad.sql
git add src/main/java/com/gestionfuncionarios/
git add src/test/java/com/gestionfuncionarios/

# 4. Confirmar que solo se agregaron archivos de codigo y documentacion (sin contraseñas)
git status

# 5. Registrar el commit
git commit -m "feat: implementacion completa de autenticacion BCrypt, roles RBAC y gestion de usuarios"

# 6. Enviar cambios a la rama principal en GitHub
git push origin main
```

---

## 17. Integrantes del Proyecto

| Nombre completo | Código estudiantil | Rol |
|---|---|---|
| Santiago Palacio | [Código] | Desarrollador principal (Proyecto individual) |

---

*Proyecto desarrollado para la asignatura **Desarrollo de Software Seguro** — 2026.*
