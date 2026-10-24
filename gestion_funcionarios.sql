-- =====================================================================
-- SCRIPT DE CREACION Y CARGA INICIAL: gestion_funcionarios
-- Sistema de Gestion de Funcionarios - Desarrollo de Software Seguro
-- NOTA IMPORTANTE:
--   Este script esta destinado para la instalacion en entornos NUEVOS.
--   NO ejecutar sobre bases de datos existentes que ya contengan estas
--   tablas para evitar duplicidad de llaves primarias o errores.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS gestion_funcionarios
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gestion_funcionarios;

-- ---------------------------------------------------------------------
-- 1. TABLA PRINCIPAL: funcionarios
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS funcionarios (
    id_funcionario INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    estado_civil VARCHAR(30),
    sexo VARCHAR(20),
    direccion VARCHAR(200),
    telefono VARCHAR(20),
    fecha_nacimiento DATE,
    INDEX idx_funcionario_documento (numero_documento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 2. TABLA RELACIONADA: grupo_familiar
--    Relacion 1 a N con funcionarios mediante clave foranea id_funcionario.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS grupo_familiar (
    id_familiar INT AUTO_INCREMENT PRIMARY KEY,
    id_funcionario INT NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    parentesco VARCHAR(50) NOT NULL,
    fecha_nacimiento DATE,
    INDEX idx_familiar_funcionario (id_funcionario),
    CONSTRAINT fk_familiar_funcionario
        FOREIGN KEY (id_funcionario)
        REFERENCES funcionarios(id_funcionario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 3. TABLA RELACIONADA: estudios
--    Relacion 1 a N con funcionarios mediante clave foranea id_funcionario.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estudios (
    id_estudio INT AUTO_INCREMENT PRIMARY KEY,
    id_funcionario INT NOT NULL,
    universidad VARCHAR(150) NOT NULL,
    nivel_academico VARCHAR(100) NOT NULL,
    titulo_obtenido VARCHAR(150),
    INDEX idx_estudio_funcionario (id_funcionario),
    CONSTRAINT fk_estudio_funcionario
        FOREIGN KEY (id_funcionario)
        REFERENCES funcionarios(id_funcionario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 4. DATOS INICIALES DE PRUEBA (Solo para instalaciones nuevas)
-- ---------------------------------------------------------------------
INSERT IGNORE INTO funcionarios
(id_funcionario, tipo_documento, numero_documento, nombres, apellidos,
 estado_civil, sexo, direccion, telefono, fecha_nacimiento)
VALUES
(1, 'CC', '1000000001', 'Ana', 'Gomez Perez', 'Soltera',
 'Femenino', 'Calle 10 # 5-20', '3001112233', '1995-04-12'),
(2, 'CC', '1000000002', 'Carlos', 'Rodriguez Lopez', 'Casado',
 'Masculino', 'Carrera 8 # 12-30', '3002223344', '1988-09-25'),
(3, 'CC', '1000000003', 'Maria', 'Torres Diaz', 'Soltera',
 'Femenino', 'Calle 20 # 7-15', '3003334455', '1992-01-08');

INSERT IGNORE INTO grupo_familiar
(id_familiar, id_funcionario, nombres, apellidos, parentesco, fecha_nacimiento)
VALUES
(1, 1, 'Laura', 'Gomez Ruiz', 'Hermana', '2000-06-15'),
(2, 2, 'Pedro', 'Rodriguez Mora', 'Hijo', '2015-03-20'),
(3, 3, 'Sofia', 'Torres Diaz', 'Madre', '1965-11-02');

INSERT IGNORE INTO estudios
(id_estudio, id_funcionario, universidad, nivel_academico, titulo_obtenido)
VALUES
(1, 1, 'Universidad Nacional', 'Universitario', 'Ingenieria de Sistemas'),
(2, 2, 'Universidad de Antioquia', 'Especializacion', 'Gestion Empresarial'),
(3, 3, 'Universidad del Valle', 'Universitario', 'Administracion de Empresas');
