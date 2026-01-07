# Tests de Migraciones - Guía de Uso

## 📋 Descripción

Suite de tests automatizados con JUnit 4 para validar el sistema de migraciones.

---

## 🏗️ Estructura de Tests

```
src/test/java/com/Database/
├── BaseTest.java              - Clase base con setup y cleanup
├── MigrationRecordTest.java   - Tests de registro de migraciones
└── SchemaTest.java            - Tests de creación de tablas
```

---

## 🚀 Ejecutar Tests

### **Ejecutar todos los tests:**

```bash
mvn test
```

### **Ejecutar un test específico:**

```bash
# Test de MigrationRecord
mvn test -Dtest=MigrationRecordTest

# Test de Schema
mvn test -Dtest=SchemaTest
```

### **Ejecutar un método específico:**

```bash
mvn test -Dtest=MigrationRecordTest#testRecordMigration
mvn test -Dtest=SchemaTest#testCreateSimpleTable
```

### **Ver resultados detallados:**

```bash
mvn test -X
```

---

## 📊 Tests Disponibles

### **MigrationRecordTest** (10 tests)

| Test | Descripción |
|------|-------------|
| `testCreateMigrationsTable` | Verifica creación de tabla migrations |
| `testRecordMigration` | Registra una migración correctamente |
| `testPreventDuplicates` | Evita duplicados (debe fallar) |
| `testDeleteMigration` | Elimina una migración |
| `testGetExecutedMigrations` | Obtiene todas las migraciones |
| `testGetNextBatchNumber` | Calcula siguiente batch |
| `testGetLastBatchMigrations` | Obtiene último batch |
| `testIsMigrationExecuted` | Verifica si migración existe |
| `testExecutionTime` | Registra tiempo de ejecución |
| `testEmptyBatch` | Maneja batches vacíos |

### **SchemaTest** (7 tests)

| Test | Descripción |
|------|-------------|
| `testCreateSimpleTable` | Crea tabla simple |
| `testDropTableIfExists` | Elimina tabla existente |
| `testDropTableThatDoesNotExist` | Elimina tabla inexistente (sin error) |
| `testCreateTableWithAllColumnTypes` | Crea tabla con todos los tipos |
| `testCreateTableWithModifiers` | Usa modificadores (notNull, unique, etc) |
| `testCreateTableThatAlreadyExists` | Error al duplicar tabla |
| `testInsertDataAfterCreation` | Inserta datos en tabla creada |

---

## 🧪 Cómo Funcionan los Tests

### **1. BaseTest (Clase Base)**

Todos los tests extienden de `BaseTest`, que proporciona:

- **@Before (setUp)**:
  - Establece conexión a BD
  - Crea tabla `migrations` si no existe
  - Limpia datos de pruebas anteriores

- **@After (tearDown)**:
  - Limpia tablas de prueba
  - Cierra conexión

- **Métodos útiles**:
  - `tableExists(String)` - Verifica si tabla existe
  - `countRows(String)` - Cuenta filas en tabla
  - `cleanupTestData()` - Limpia tablas de prueba

### **2. Aislamiento de Tests**

Cada test:
- Se ejecuta de forma independiente
- Tiene su propia limpieza antes y después
- Usa nombres de tabla con prefijo `test_` o versión `9999%`
- No afecta datos de producción

### **3. Convenciones**

- **Tablas de prueba**: Prefijo `test_` (ej: `test_usuarios`)
- **Versiones de prueba**: `9999xxxx` (ej: `99990101`)
- **Limpieza automática**: Se eliminan al terminar cada test

---

## ✅ Ejemplo de Salida Exitosa

```bash
$ mvn test -Dtest=MigrationRecordTest

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.Database.MigrationRecordTest
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.453 sec

Results :

Tests run: 10, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## ❌ Ejemplo de Salida con Error

```bash
$ mvn test -Dtest=SchemaTest

Tests run: 7, Failures: 1, Errors: 0, Skipped: 0

FAILURE!
testCreateSimpleTable(com.Database.SchemaTest)
  Expected: true
  Actual: false

[ERROR] Tests run: 7, Failures: 1, Errors: 0, Skipped: 0
```

---

## 🔧 Troubleshooting

### **Error: No puede conectar a la base de datos**

**Problema:**
```
java.sql.SQLException: Communications link failure
```

**Solución:**
- Verifica que la BD esté corriendo
- Revisa las credenciales en `CConexion.java`
- Verifica el puerto (usualmente 3306 para MySQL/MariaDB)

---

### **Error: Tabla ya existe**

**Problema:**
```
SQLException: Table 'test_usuarios' already exists
```

**Solución:**
```bash
# Limpiar manualmente la BD de prueba
mysql -u usuario -p

USE nombre_base_datos;
DROP TABLE IF EXISTS test_usuarios;
DROP TABLE IF EXISTS test_productos;
DROP TABLE IF EXISTS test_categorias;
DROP TABLE IF EXISTS test_ventas;
DELETE FROM migrations WHERE version LIKE '9999%';
```

---

### **Error: JUnit no encontrado**

**Problema:**
```
[ERROR] cannot find symbol: class Test
```

**Solución:**
```bash
# Reinstalar dependencias
mvn clean install
```

---

## 📝 Escribir Nuevos Tests

### **Ejemplo de Test Básico:**

```java
package com.Database;

import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.SQLException;

public class MiNuevoTest extends BaseTest {

    @Test
    public void testAlgo() throws SQLException {
        // Preparar
        String tableName = "test_mi_tabla";

        // Ejecutar
        Schema.create(tableName, table -> {
            table.id();
            table.string("campo");
        }, connection);

        // Verificar
        assertTrue("La tabla debe existir", tableExists(tableName));
    }
}
```

### **Assertions Útiles:**

```java
// Verificar verdadero/falso
assertTrue("mensaje", condicion);
assertFalse("mensaje", condicion);

// Verificar igualdad
assertEquals("mensaje", esperado, actual);

// Verificar no nulo
assertNotNull("mensaje", objeto);

// Verificar que lanza excepción
@Test(expected = SQLException.class)
public void testError() throws SQLException {
    // código que debe lanzar SQLException
}
```

---

## 🎯 Mejores Prácticas

### ✅ **Hacer:**

- Usar nombres descriptivos para tests
- Limpiar después de cada test
- Usar prefijo `test_` para tablas de prueba
- Verificar un solo concepto por test
- Agregar mensajes claros en assertions

### ❌ **Evitar:**

- Depender de orden de ejecución de tests
- Usar datos de producción
- Tests largos y complejos
- Compartir estado entre tests
- Hardcodear valores mágicos

---

## 📈 Cobertura de Tests

Para ver cobertura de tests, puedes usar JaCoCo:

```xml
<!-- Agregar en pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Ejecutar:
```bash
mvn test jacoco:report
```

Ver reporte en: `target/site/jacoco/index.html`

---

## 🔄 Integración Continua

Para ejecutar tests automáticamente en CI/CD:

### **GitHub Actions:**

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Run tests
        run: mvn test
```

---

## 📞 Ayuda

Si tienes problemas con los tests:

1. Verifica que tu BD de prueba esté configurada
2. Ejecuta `mvn clean test` para limpiar y ejecutar
3. Revisa los logs detallados con `mvn test -X`
4. Asegúrate que JUnit 4.11 esté en el pom.xml

---

## 📚 Recursos

- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [AssertJ (alternativa a assertions)](https://assertj.github.io/doc/)
