-- techdam_completo.sql
-- Crea la BD, tablas, índices, datos de prueba, procedimientos y función

DROP DATABASE IF EXISTS techdam;
CREATE DATABASE techdam CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE techdam;

-- TABLA empleados
CREATE TABLE empleados (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL,
                           salario DECIMAL(12,2) NOT NULL DEFAULT 0,
                           departamento VARCHAR(50),
                           activo TINYINT(1) DEFAULT 1,
                           fecha_contratacion DATE,
                           UNIQUE INDEX ux_empleados_nombre (nombre)
);

-- TABLA proyectos
CREATE TABLE proyectos (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(150) NOT NULL,
                           presupuesto DECIMAL(14,2) NOT NULL DEFAULT 0,
                           fecha_inicio DATE,
                           fecha_fin DATE,
                           activo TINYINT(1) DEFAULT 1,
                           UNIQUE INDEX ux_proyectos_nombre (nombre)
);

-- TABLA asignaciones (relación N:M)
CREATE TABLE asignaciones (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              empleado_id INT NOT NULL,
                              proyecto_id INT NOT NULL,
                              fecha_asignacion DATE NOT NULL,
                              horas_semana INT DEFAULT 0,
                              FOREIGN KEY (empleado_id) REFERENCES empleados(id) ON DELETE CASCADE,
                              FOREIGN KEY (proyecto_id) REFERENCES proyectos(id) ON DELETE CASCADE,
                              INDEX idx_asignaciones_empleado (empleado_id),
                              INDEX idx_asignaciones_proyecto (proyecto_id)
);

-- Datos de prueba (al menos 5 registros por tabla)
INSERT INTO empleados (nombre, salario, departamento, activo, fecha_contratacion) VALUES
                                                                                      ('Ana López', 1500.00, 'Finanzas', 1, '2019-06-10'),
                                                                                      ('Luis García', 1800.00, 'Desarrollo', 1, '2018-03-20'),
                                                                                      ('Marta Ruiz', 2000.00, 'Desarrollo', 1, '2020-01-15'),
                                                                                      ('Pedro Gómez', 1750.00, 'Ventas', 1, '2017-11-01'),
                                                                                      ('Laura Fernández', 2200.00, 'Proyectos', 1, '2021-09-01');

INSERT INTO proyectos (nombre, presupuesto, fecha_inicio, fecha_fin, activo) VALUES
                                                                                 ('Proyecto Alpha', 50000.00, '2024-01-01', '2024-12-31', 1),
                                                                                 ('Proyecto Beta', 30000.00, '2024-03-01', '2024-09-30', 1),
                                                                                 ('Proyecto Gamma', 15000.00, '2024-05-01', '2024-11-30', 1),
                                                                                 ('Proyecto Delta', 8000.00, '2024-02-15', '2024-08-15', 1),
                                                                                 ('Proyecto Epsilon', 12000.00, '2024-06-01', '2024-12-01', 1);

INSERT INTO asignaciones (empleado_id, proyecto_id, fecha_asignacion, horas_semana) VALUES
                                                                                        (1, 1, '2024-01-10', 20),
                                                                                        (2, 1, '2024-02-01', 30),
                                                                                        (3, 2, '2024-03-05', 25),
                                                                                        (4, 3, '2024-04-01', 15),
                                                                                        (5, 2, '2024-05-01', 20),
                                                                                        (1, 5, '2024-06-10', 10),
                                                                                        (2, 3, '2024-07-01', 5),
                                                                                        (3, 4, '2024-07-15', 10);

-- PROCEDIMIENTO 1: actualizar_salario_departamento (ejemplo del enunciado)
DELIMITER $$
CREATE PROCEDURE actualizar_salario_departamento(
    IN p_departamento VARCHAR(50),
    IN p_porcentaje DECIMAL(5,2),
    OUT p_empleados_actualizados INT
)
BEGIN
UPDATE empleados
SET salario = salario * (1 + p_porcentaje / 100)
WHERE departamento = p_departamento AND activo = TRUE;
SET p_empleados_actualizados = ROW_COUNT();
END$$
DELIMITER ;

-- PROCEDIMIENTO 2: crear_asignacion (inserta asignación y devuelve id o 0 si falla)
DELIMITER $$
CREATE PROCEDURE crear_asignacion(
    IN p_empleado_id INT,
    IN p_proyecto_id INT,
    IN p_fecha DATE,
    IN p_horas INT,
    OUT p_asignacion_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
ROLLBACK;
SET p_asignacion_id = 0;
END;

START TRANSACTION;
INSERT INTO asignaciones (empleado_id, proyecto_id, fecha_asignacion, horas_semana)
VALUES (p_empleado_id, p_proyecto_id, p_fecha, p_horas);
SET p_asignacion_id = LAST_INSERT_ID();
COMMIT;
END$$
DELIMITER ;

-- FUNCION: contar_empleados_proyecto
DELIMITER $$
CREATE FUNCTION contar_empleados_proyecto(p_proyecto_id INT) RETURNS INT
    DETERMINISTIC
BEGIN
    DECLARE cnt INT;
SELECT COUNT(DISTINCT empleado_id) INTO cnt
FROM asignaciones
WHERE proyecto_id = p_proyecto_id;
RETURN cnt;
END$$
DELIMITER ;

-- Índices adicionales si se desea (ya hay índices en FK)