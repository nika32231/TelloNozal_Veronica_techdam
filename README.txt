# Proyecto TechDAM - JDBC Avanzado
Fecha: 2025-11-16
Autor: nika32231

---

## Requisitos previos

- Java 17 instalado
- Maven instalado
- Docker y Docker Compose instalados (para levantar MySQL + phpMyAdmin)
- IntelliJ IDEA (recomendado) o cualquier IDE compatible con Maven

---

## Contenido del repositorio (resumen)

- `docker-compose.yml` — MySQL + phpMyAdmin
- `techdam_completo.sql` — script SQL (crea BD `techdam`, tablas, índices, datos, 2 procedimientos y 1 función)
- `pom.xml` — configuración Maven (HikariCP, MySQL connector, slf4j)
- `src/main/java/...` — código Java:
  - `com.techdam.config.DatabaseConfigPool` — HikariCP + lectura `src/main/resources/db.properties`
  - Modelos: `Empleado`, `Proyecto`, `Asignacion`
  - DAOs: `EmpleadoDAOImpl`, `ProyectoDAOImpl` (CRUD con PreparedStatement)
  - Servicios: `ProcedimientosService`, `TransaccionesService`
  - `com.techdam.Main` — clase demo que ejecuta ejemplos
- `src/main/resources/db.properties` — configuración de conexión (host, usuario, contraseña, pool)

---

## 1) Levantar MySQL + phpMyAdmin con Docker

Desde la carpeta donde está `docker-compose.yml`:

1. Levantar contenedores:
   - Linux / macOS / WSL / PowerShell:
     ```
     docker-compose up -d
     ```
   - Windows (cmd / PowerShell): usa la ruta correcta si no estás en la carpeta.

2. Verifica contenedores:
   ```
   docker ps
   ```

3. Accede a phpMyAdmin:
   http://localhost:8080
   Usuario: `root`
   Contraseña: `root123`

> Nota: el `docker-compose.yml` incluido crea la base de datos `techdam` automáticamente. Si tu `docker-compose` inicial crea `testdb`, revisa/ajusta el archivo `db.properties` para apuntar a la BD correcta.

---

## 2) Ejecutar el script SQL (crear tablas y datos)

Opciones:

- Desde phpMyAdmin:
  - Abre `techdam` (o crea la BD si no existe).
  - Importa `techdam_completo.sql`.

- Desde la terminal (container Docker):
  ```
  docker exec -i mysql-container mysql -u root -p root123 < path/to/techdam_completo.sql
  ```
  Asegúrate de que `mysql-container` es el nombre del contenedor (según el `docker-compose.yml`).

---

## 3) Configurar la conexión JDBC (db.properties)

Archivo: `src/main/resources/db.properties`

Valores por defecto incluidos en el proyecto:
```
jdbc.url=jdbc:mysql://localhost:3306/techdam?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=root123
jdbc.driverClassName=com.mysql.cj.jdbc.Driver
...
```

Si tu MySQL está en otro host/puerto, actualiza `jdbc.url` y credenciales. Si usas Docker en otro host, reemplaza `localhost` por la IP/host correspondiente.

---

## 4) Compilar y ejecutar el proyecto

Recomendado: abrir en IntelliJ como proyecto Maven y ejecutar la clase `com.techdam.Main`.

Opciones por línea de comandos:

1. Compilar:
```
mvn clean package
```

2. Ejecutar desde IntelliJ:
- Run -> Edit Configurations -> Application -> Main class: `com.techdam.Main`
- Ejecutar la configuración.

3. Ejecutar con Maven (si tienes el plugin exec disponible):
```
mvn exec:java -Dexec.mainClass="com.techdam.Main"
```
(Si no tienes `exec` configurado, ejecútalo desde IntelliJ.)

---

## 5) Qué hace `Main` (ejemplo)

- Inserta un empleado de prueba (si el nombre ya existe, el DAO recupera el id existente para evitar excepción).
- Lista todos los empleados (muestra que funciona el CRUD SELECT).
- Crea un proyecto de prueba (si ya existe, el DAO recupera el id existente).
- Ejecuta una transferencia de presupuesto (transacción con commit/rollback).
- Llama a procedimientos almacenados (`actualizar_salario_departamento`, `crear_asignacion`) mediante `CallableStatement`.
- Intenta insertar asignaciones en batch (método `asignarEmpleadosBatch`). Por defecto la versión antigua usada en `Main` aborta si hay ids faltantes; hay también una sobrecarga con control `abortOnMissing`.

---

## 6) Errores comunes y soluciones

1. Error SLF4J: AbstractMethodError (ej. `org.slf4j.simple.SimpleServiceProvider does not define ... getRequesteApiVersion`)
   - Causa: conflicto/incompatibilidad entre versión de `slf4j-api` y binding (`slf4j-simple`).
   - Solución: asegurar versiones consistentes en `pom.xml`. Ejecuta `mvn dependency:tree` y excluir bindings duplicados si alguna dependencia trae una versión distinta.

2. Duplicate entry (índice UNIQUE) al insertar `Empleado` o `Proyecto`
   - Causa: ya existe un registro con el mismo nombre y hay un índice UNIQUE (`ux_empleados_nombre` o `ux_proyectos_nombre`).
   - Solución:
     - Borrar el registro duplicado manualmente desde phpMyAdmin:
       ```
       DELETE FROM empleados WHERE nombre = 'Prueba Usuario';
       DELETE FROM proyectos WHERE nombre = 'Proyecto Demo';
       ```
     - O dejar que los DAOs manejen duplicados: el código captura `SQLIntegrityConstraintViolationException` y devuelve el id existente en vez de fallar (implementación incluida).
     - Alternativa: usar nombres únicos en pruebas (p. ej. añadir timestamp).

3. `BatchUpdateException` por FK (empleado inexistente)
   - Causa: intentas insertar en `asignaciones` con `empleado_id` que no existe en `empleados`.
   - Solución:
     - Validar ids antes del batch (método `asignarEmpleadosBatch` implementado hace esto).
     - O ejecutar fila a fila e ignorar errores (método `asignarEmpleadosIgnorandoErrores` disponible).
     - Evitar ids de prueba inválidos (ej. `9999`) o borrar/crear empleados necesarios.

4. Problemas de conexión desde Docker:
   - Si `localhost:3306` no funciona desde tu IDE, prueba con la IP del host Docker (WSL2/Windows pueden requerir `127.0.0.1` o la IP de la interfaz). Con `docker-compose` mapeando `3306:3306`, `localhost` suele funcionar.

---

## 7) Notas sobre comportamiento y configuraciones relevantes

- Pool HikariCP:
  - Configurado en `src/main/resources/db.properties`.
  - Se cierra explicitamente al final de `Main` con `DatabaseConfigPool.close()`.
- Seguridad:
  - Todas las consultas usan `PreparedStatement` (protección contra inyección SQL).
- Procedimientos y funciones:
  - `ProcedimientosService` muestra cómo invocar procedimientos con `CallableStatement` y manejar parámetros IN/OUT.
  - La función `contar_empleados_proyecto` se invoca con `SELECT contar_empleados_proyecto(?)`.
- Transacciones:
  - `transferirPresupuesto` usa transacción manual (autoCommit=false, commit/rollback).
  - `asignarEmpleadosBatch` realiza validación previa y ejecuta batch dentro de una transacción; puede abortar si hay empleados faltantes o insertar solo los válidos (según parámetro).
- Comportamiento al re-ejecutar `Main`:
  - Los DAOs manejan duplicados devolviendo ids existentes, por lo que no deberías tener fallos por re-ejecuciones con los mismos nombres. Aun así, algunas operaciones (batch con FK inválida) deberán ajustarse o limpiarse manualmente en la BD.

---

## 8) Qué añadir a la documentación final (entregable)

Según el enunciado deberás entregar:
- Diagrama ER (3 tablas + PK/FK)
- Capturas: tablas en MySQL Workbench / phpMyAdmin, CRUD empleados/proyectos, invocación de procedimiento, transacción con commit, transacción con rollback, evidencia del pool (logs)
- Explicaciones técnicas (por qué PreparedStatement, ventajas del pool, cuándo usar procedimientos, importancia del control de transacciones)
- Problemas encontrados y soluciones (ej.: SLF4J, duplicados, FK en batch)

---

## 9) Comandos útiles rápidos

Levantar DB:
```
docker-compose up -d
docker ps
```

Importar SQL:
```
docker exec -i mysql-container mysql -u root -p root123 < techdam_completo.sql
```

Compilar:
```
mvn clean package
```

Ejecutar desde IntelliJ:
- Run -> `com.techdam.Main`

Eliminar datos de prueba (phpMyAdmin o CLI):
```
DELETE FROM asignaciones WHERE proyecto_id = <id>;
DELETE FROM empleados WHERE nombre = 'Prueba Usuario';
DELETE FROM proyectos WHERE nombre = 'Proyecto Demo';
```

Ver dependencias SLF4J:
```
mvn dependency:tree | grep slf4j -n
```
