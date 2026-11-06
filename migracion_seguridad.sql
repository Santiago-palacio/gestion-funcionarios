-- =====================================================================
-- SCRIPT DE MIGRACION: Autenticacion y Autorizacion por Roles
-- Sistema de Gestion de Funcionarios - Segunda Entrega
-- Asignatura: Desarrollo de Software Seguro
-- =====================================================================
-- ADVERTENCIA IMPORTANTE:
--   Este script es una migracion NO destructiva.
--   - NO elimina ninguna tabla existente (funcionarios, grupo_familiar, estudios).
--   - NO borra datos de funcionarios registrados.
--   - NO modifica la base de datos gestionproyectos.
--   - Usa CREATE TABLE IF NOT EXISTS para evitar errores en re-ejecuciones.
--   - Los usuarios de demostracion usan hashes BCrypt validos generados
--     con factor de coste 12 y sal criptografica aleatoria.
-- =====================================================================
-- CREDENCIALES DE DEMOSTRACION INCLUIDAS:
--   1. Rol ADMINISTRADOR:
--      - Usuario:    admin
--      - Contraseña: Admin123*
--      - Permisos:   Acceso total (consultar, registrar, actualizar, eliminar)
--
--   2. Rol CONSULTA:
--      - Usuario:    consulta
--      - Contraseña: Consulta123*
--      - Permisos:   Solo lectura (consultar funcionarios, ver familiares y estudios)
-- =====================================================================
-- INSTRUCCIONES DE EJECUCION DESDE MYSQL WORKBENCH:
--   1. Abra MySQL Workbench y conectese a su instancia MySQL 8.4 local.
--   2. Vaya al menu: File > Open SQL Script...
--   3. Seleccione este archivo: database/migracion_seguridad.sql
--   4. Presione el icono del rayo o presione [Ctrl + Shift + Enter] para ejecutar.
--   5. Verifique en la parte inferior (Output) que todas las sentencias esten en verde (OK).
-- =====================================================================

USE gestion_funcionarios;

-- ---------------------------------------------------------------------
-- 1. TABLA: roles
--    Catalogo de roles de seguridad del sistema.
--    id_rol 1 = ADMINISTRADOR, id_rol 2 = CONSULTA
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id_rol      INT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(50)  NOT NULL,
    descripcion VARCHAR(255) DEFAULT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id_rol),
    CONSTRAINT uq_roles_nombre UNIQUE (nombre)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Catalogo de roles de seguridad del sistema';

-- ---------------------------------------------------------------------
-- 2. DATOS INICIALES: roles
--    INSERT IGNORE evita duplicados si el script se vuelve a ejecutar.
-- ---------------------------------------------------------------------
INSERT IGNORE INTO roles (id_rol, nombre, descripcion) VALUES
(1, 'ADMINISTRADOR', 'Acceso total: consulta, registro, modificacion y eliminacion de funcionarios y relaciones'),
(2, 'CONSULTA',      'Acceso de solo lectura: visualizacion de funcionarios, familiares y estudios sin privilegios de modificacion');

-- ---------------------------------------------------------------------
-- 3. TABLA: usuarios
--    Almacena las cuentas de acceso al sistema con contraseñas hasheadas en BCrypt.
--    - password_hash: hash BCrypt de 60 caracteres con sal aleatoria.
--    - activo: permite habilitar o deshabilitar usuarios sin borrar registros.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario     INT           NOT NULL AUTO_INCREMENT,
    username       VARCHAR(100)  NOT NULL,
    password_hash  VARCHAR(255)  NOT NULL COMMENT 'Hash BCrypt de la contraseña. Nunca texto plano.',
    id_rol         INT           NOT NULL,
    activo         TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1=activo, 0=inactivo',
    fecha_creacion TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuarios    PRIMARY KEY (id_usuario),
    CONSTRAINT uq_username    UNIQUE      (username),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol)
        REFERENCES roles (id_rol)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_usuario_username (username),
    INDEX idx_usuario_activo   (activo)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Cuentas de acceso al sistema con autenticacion BCrypt';

-- ---------------------------------------------------------------------
-- 4. USUARIOS DE DEMOSTRACION
--    Hashes BCrypt generados con PasswordHasher (coste 12 y sal criptografica).
--    ON DUPLICATE KEY UPDATE garantiza que re-ejecutar el script no duplique registros.
-- ---------------------------------------------------------------------
INSERT INTO usuarios (username, password_hash, id_rol, activo)
VALUES
('admin',    '$2a$12$wrQDu91ctVzc8qcWfR3A/eoNk7a92k.OrW0WgccfPeP1pHgc1hgyG', 1, 1),
('consulta', '$2a$12$qm8gkmDql7jxytorcyHrq.ZRadmEyPLeZb0/.M9rswQpUY/DDBvKC', 2, 1)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    id_rol        = VALUES(id_rol),
    activo        = VALUES(activo);

-- ---------------------------------------------------------------------
-- 5. VERIFICACION DE ESTADO
-- ---------------------------------------------------------------------
SELECT 'roles' AS tabla, COUNT(*) AS total_registros FROM roles
UNION ALL
SELECT 'usuarios' AS tabla, COUNT(*) AS total_registros FROM usuarios;

SELECT u.id_usuario, u.username, r.nombre AS rol, u.activo, u.fecha_creacion
FROM usuarios u
INNER JOIN roles r ON u.id_rol = r.id_rol;

