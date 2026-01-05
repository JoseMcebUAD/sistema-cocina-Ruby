# Sistema de Migraciones - Guía Completa

## 📋 Tabla de Contenidos
- [Introducción](#introducción)
- [Características](#características)
- [Comandos Disponibles](#comandos-disponibles)
- [Estructura de Archivos](#estructura-de-archivos)
- [Crear una Nueva Migración](#crear-una-nueva-migración)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Manejo de Errores](#manejo-de-errores)

---

## 🎯 Introducción

Este sistema de migraciones permite gestionar la estructura de la base de datos de forma programática, con control de versiones y rollback automático.

## ✨ Características

✅ **Lectura automática** de archivos de migración desde el directorio `migrations/`
✅ **Ordenamiento automático** por fecha (formato YYYYMMDD)
✅ **Tabla de registro** que guarda todas las migraciones ejecutadas
✅ **Transacciones** con rollback automático en caso de error
✅ **Batches** para agrupar migraciones ejecutadas juntas
✅ **Preservación de datos** al recrear migraciones
✅ **Mensajes de error detallados** con información del archivo y SQL

---

## 🚀 Comandos Disponibles

### 1. `up` - Ejecutar migraciones pendientes
Ejecuta todas las migraciones que aún no han sido registradas en la base de datos.

```bash
migrate.bat up
```

**¿Qué hace?**
- Lee todos los archivos del directorio `migrations/`
- Compara con la tabla `migrations` para ver cuáles faltan
- Ejecuta las pendientes en orden cronológico
- Registra cada migración exitosa en la tabla

---

### 2. `down` - Revertir último batch
Revierte el último grupo de migraciones ejecutadas.

```bash
migrate.bat down
```

**¿Qué hace?**
- Obtiene las migraciones del último batch
- Las revierte en orden inverso
- Elimina sus registros de la tabla `migrations`

---

### 3. `reup` - Recrear todas las migraciones
Elimina y vuelve a crear todas las tablas (⚠️ **ELIMINA TODOS LOS DATOS**).

```bash
migrate.bat reup
```

**¿Qué hace?**
1. Revierte TODAS las migraciones (elimina todas las tablas)
2. Ejecuta TODAS las migraciones de nuevo (crea tablas limpias)

**⚠️ PRECAUCIÓN:** Este comando elimina todos los datos de las tablas.

---

### 4. `reup-with-data` - Recrear preservando datos
Elimina y recrea todas las tablas, pero preserva los datos existentes.

```bash
migrate.bat reup-with-data
```

**¿Qué hace?**
1. **Respalda** todos los datos de todas las tablas en memoria
2. Revierte TODAS las migraciones (elimina tablas)
3. Ejecuta TODAS las migraciones (crea tablas nuevas)
4. **Restaura** los datos compatibles

**Manejo inteligente de datos:**
- Si una columna ya no existe, ignora ese dato
- Si cambió el tipo de dato y causa error, omite esa fila
- Si cambió el nombre de columna, no la inserta
- Continúa con la siguiente fila aunque falle una

---

### 5. `status` - Ver estado de migraciones
Muestra qué migraciones están ejecutadas y cuáles están pendientes.

```bash
migrate.bat status
```

**Salida:**
```
📋 Migraciones ejecutadas:

VERSION      CLASE                                    TIEMPO     BATCH
---------------------------------------------------------------------------
20250104     CreateUsuariosTable                      125ms      #1
20250104     CreateProductosTable                     98ms       #1

⏳ Migraciones pendientes:

  - 20250105-CreateVentasTable
```

---

## 📁 Estructura de Archivos

### Ubicación de migraciones
```
src/main/java/com/Database/migrations/
├── 20250104-CreateUsuariosTable.java
├── 20250104-CreateProductosTable.java
└── 20250105-CreateVentasTable.java
```

### Formato de nombre de archivo
```
YYYYMMDD-NombreClase.java
```

**Ejemplos válidos:**
- `20250104-CreateUsuariosTable.java` ✅
- `20250105-AddRolesTable.java` ✅
- `20250106-ModifyUsuariosAddAge.java` ✅

**Ejemplos inválidos:**
- `CreateUsuarios.java` ❌ (falta fecha)
- `2025-01-04-CreateUsuarios.java` ❌ (formato de fecha incorrecto)
- `CreateUsuarios-20250104.java` ❌ (orden incorrecto)

---

## 📝 Crear una Nueva Migración

### Paso 1: Crear el archivo
Crea un archivo en `src/main/java/com/Database/migrations/` con el formato:
```
YYYYMMDD-NombreDescriptivo.java
```

### Paso 2: Escribir el código

```java
package com.Database;

import java.sql.SQLException;

public class CreateCategoriasTable extends Migration {

    @Override
    public void up() {
        try {
            Schema.create("categorias", table -> {
                table.id();
                table.string("nombre", 100).notNull().unique();
                table.text("descripcion").nullable();
                table.bool("activa").defaultValue("1");
                table.timestamps();
            }, conexion);

            System.out.println("    ✓ Tabla 'categorias' creada");

        } catch (SQLException e) {
            throw new RuntimeException("Error al crear tabla categorias: " + e.getMessage(), e);
        }
    }

    @Override
    public void down() {
        try {
            Schema.dropIfExists("categorias", conexion);
            System.out.println("    ✓ Tabla 'categorias' eliminada");

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar tabla categorias: " + e.getMessage(), e);
        }
    }
}
```

### Paso 3: Ejecutar
```bash
migrate.bat up
```

---

## 💡 Ejemplos de Uso

### Ejemplo 1: Desarrollo inicial
Estás empezando el proyecto y necesitas crear las tablas:

```bash
# Crear las migraciones
migrate.bat up
```

### Ejemplo 2: Modificaste una migración
Ya ejecutaste migraciones pero modificaste una. Quieres recrearlas:

```bash
# Recrear sin datos (desarrollo)
migrate.bat reup
```

### Ejemplo 3: Modificaste migración con datos de prueba
Tienes datos de prueba que quieres mantener:

```bash
# Recrear preservando datos
migrate.bat reup-with-data
```

### Ejemplo 4: Error en una migración
Una migración falló, quieres revertir el batch:

```bash
# Ver estado
migrate.bat status

# Revertir último batch
migrate.bat down

# Corregir el archivo de migración
# ...

# Intentar de nuevo
migrate.bat up
```

### Ejemplo 5: Agregar nueva tabla
Necesitas agregar una tabla nueva:

1. Crear archivo: `20250105-CreateVentasTable.java`
2. Ejecutar: `migrate.bat up`
3. Solo se ejecutará la nueva migración

---

## 🎓 Tipos de Columnas Disponibles

```java
// ID autoincremental
table.id();                           // id_[nombre_tabla]
table.id("custom_id");                // custom_id

// Texto
table.string("nombre");               // VARCHAR(255)
table.string("nombre", 100);          // VARCHAR(100)
table.text("descripcion");            // TEXT

// Números
table.integer("edad");                // INT
table.bigInteger("count");            // BIGINT
table.decimal("precio", 10, 2);       // DECIMAL(10,2)

// Booleanos
table.bool("activo");                 // TINYINT(1) DEFAULT 0

// Fechas
table.date("nacimiento");             // DATE
table.datetime("registro");           // DATETIME
table.timestamp("logged_at");         // TIMESTAMP
table.timestamps();                   // created_at, updated_at

// Modificadores
.notNull()                            // NOT NULL
.nullable()                           // NULL
.unique()                             // UNIQUE
.defaultValue("valor")                // DEFAULT valor

// Relaciones
table.foreign("user_id", "users");            // FK a users(id)
table.foreign("cat_id", "cats", "cat_id");    // FK a cats(cat_id)
.onDeleteCascade()                            // ON DELETE CASCADE
.onDeleteSetNull()                            // ON DELETE SET NULL

// Índices
table.index("email", "nombre");       // INDEX
```

---

## ⚠️ Manejo de Errores

### Error en SQL
Si hay un error en el SQL, verás:

```
❌ ERROR EN MIGRACIÓN: 20250104-CreateUsuariosTable
📄 Archivo: C:\...\migrations\20250104-CreateUsuariosTable.java
💬 Mensaje: Table 'usuarios' already exists

Stack trace:
...
```

**Solución:** La transacción se revirtió automáticamente. Corrige el error y vuelve a ejecutar.

### Archivo no compilado
```
No se pudo encontrar la clase compilada: com.Database.CreateUsuariosTable
Asegúrate de compilar el proyecto primero: mvn compile
```

**Solución:** Ejecuta `mvn compile` antes de `migrate.bat up`

### Formato de archivo incorrecto
```
⚠ Archivo ignorado (formato inválido): MiMigracion.java
  Formato esperado: YYYYMMDD-NombreClase.java
```

**Solución:** Renombra el archivo con el formato correcto.

---

## 🗃️ Tabla de Migraciones

El sistema crea automáticamente una tabla llamada `migrations`:

| Columna      | Tipo         | Descripción                           |
|--------------|--------------|---------------------------------------|
| id           | INT (PK)     | ID autoincremental                    |
| version      | VARCHAR(8)   | Fecha de la migración (YYYYMMDD)      |
| class        | VARCHAR(255) | Nombre de la clase                    |
| time_ms      | BIGINT       | Tiempo de ejecución en milisegundos   |
| batch        | INT          | Número de batch                       |
| executed_at  | TIMESTAMP    | Cuándo se ejecutó                     |

**Ejemplo de datos:**
```sql
SELECT * FROM migrations;
```

| id | version  | class                | time_ms | batch | executed_at         |
|----|----------|----------------------|---------|-------|---------------------|
| 1  | 20250104 | CreateUsuariosTable  | 125     | 1     | 2025-01-04 10:30:00 |
| 2  | 20250104 | CreateProductosTable | 98      | 1     | 2025-01-04 10:30:01 |

---

## 🔒 Transacciones

Cada migración se ejecuta dentro de una transacción:

1. `BEGIN TRANSACTION`
2. Ejecutar migración (`up()` o `down()`)
3. Registrar en tabla `migrations`
4. `COMMIT`

Si hay error en cualquier paso:
- `ROLLBACK` automático
- No se registra en la tabla
- Se muestra error detallado

Esto garantiza que la base de datos siempre esté en un estado consistente.

---

## 📞 Soporte

Si tienes problemas:
1. Verifica que compilaste: `mvn compile`
2. Revisa el formato del nombre de archivo
3. Usa `migrate.bat status` para ver el estado
4. Lee el mensaje de error completo
5. Verifica tu conexión a la base de datos

---

## 📚 Recursos Adicionales

- [Documentación de Schema.java](Database/Schema.java)
- [Ejemplos de migraciones](src/main/java/com/Database/migrations/)
- [Código fuente del MigrationRunner](src/main/java/com/Database/MigrationRunner.java)
