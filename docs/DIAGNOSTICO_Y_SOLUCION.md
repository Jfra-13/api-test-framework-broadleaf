# 🔍 DIAGNÓSTICO Y SOLUCIÓN DE ERRORES - Broadleaf API Tests

## 📋 PROBLEMAS IDENTIFICADOS

### 1. ❌ Error 404 en `/shipping/{cartId}/group` (CRÍTICO)
**Línea afectada:** `CheckoutHappyPathTest.java:122`

```
Expected status code <200> but was <404>.
```

**Causa posible:** El endpoint retorna 404 porque:
- ❓ El cartId podría ser `null` o inválido
- ❓ La sesión no se está manteniendo entre peticiones
- ❓ El endpoint no existe en el servidor Broadleaf
- ❓ Falta autenticación o headers requeridos

---

### 2. ⚠️ Warning de SLF4J
```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
```

**Causa:** Falta la dependencia de implementación de logging.

---

### 3. ⚠️ Java 17 vs Java 21
El proyecto estaba configurado para Java 17, pero tienes Java 21 instalado.

---

## ✅ SOLUCIONES APLICADAS

### 1. ✔️ **Agregué SLF4J Simple Logging**
```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>2.0.9</version>
    <scope>test</scope>
</dependency>
```
✓ El warning desaparecerá en la próxima ejecución.

---

### 2. ✔️ **Mejoré los Tests con Validaciones y Debug**

Se agregaron:
- ✓ Validaciones que verifican si los IDs se capturan correctamente
- ✓ Lanzamiento de excepciones claras si hay valores nulos
- ✓ Debug logs que muestran los valores siendo usados
- ✓ `.log().ifError()` para ver respuestas de error
- ✓ Mensajes con emojis para mejor visualización

**Ejemplo de mejora:**
```java
// ANTES - Sin validación
capturedCartId = response.jsonPath().getString("id");
System.out.println("Carrito creado con ID: " + capturedCartId);

// DESPUÉS - Con validación y debug
capturedCartId = response.jsonPath().getString("id");
if (capturedCartId == null || capturedCartId.isEmpty()) {
    throw new RuntimeException("ERROR CRÍTICO: No se pudo capturar el cartId. Respuesta: " + response.asString());
}
System.out.println("✓ Carrito creado con ID: " + capturedCartId);
```

---

### 3. ✔️ **Actualicé Java a 21**
```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>
```

---

## 🚀 PRÓXIMOS PASOS

### **OPCIÓN A: Ejecutar Tests y Ver los Debug Logs (RECOMENDADO)**

1. Recarga el proyecto en el IDE:
   - **IntelliJ IDEA**: `File` → `Invalidate Caches` → `Invalidate and Restart`
   
2. Ejecuta nuevamente el test:
   - Los mensajes de debug te mostrarán exactamente qué valores se están usando
   - Si hay error 404, verás el JSON enviado y podrás compararlo con el Swagger

3. Analiza los logs:
   ```
   DEBUG: Usando cartId = 12345
   DEBUG: Usando orderItemId = 67890
   DEBUG: Request Body = {...}
   ```

---

### **OPCIÓN B: Verificar si el Endpoint Existe**

Según el Swagger que compartiste, el endpoint debe aparecer aquí:

```
fulfillment-endpoint: Fulfillment Endpoint
├── post /shipping/{cartId}/group
    └── addFulfillmentGroupToOrder
```

✓ **AÚN NO PROBÉ** si este endpoint realmente existe en tu servidor.

**Acción:** Ve a tu servidor Broadleaf y accede a:
```
https://tu-servidor/api/v1/swagger-ui.html
```
Busca `POST /shipping/{cartId}/group` y verifica que exista.

---

### **OPCIÓN C: Verificar Autenticación**

¿Is necesario login? Algunos endpoints requieren:
- Bearer token
- Headers adicionales de autenticación
- Roles específicos

**Acción sugerida:** Agrega esto en el test para ver:
```java
Response response = given()
    .filter(sessionFilter)
    .header("Content-Type", "application/json")
    .pathParam("cartId", capturedCartId)
    .body(requestBody)
    .when()
    .post("/shipping/{cartId}/group")
    .then()
    .log().all()  // 👈 IMPRIME TODA LA RESPUESTA (headers, body, status, etc.)
    .statusCode(200)
    .extract().response();
```

---

### **OPCIÓN D: Alternativa - Usar Endpoint GET primero**

Antes de hacer POST, intenta OBTener datos del carrito con GET:

```java
@Test
public void test_04a_GetCartDetails() {
    Response response = given()
        .filter(sessionFilter)
        .pathParam("cartId", capturedCartId)
        .when()
        .get("/cart/{cartId}")
        .then()
        .log().ifError()
        .statusCode(200)
        .extract().response();
    
    System.out.println("Carrito actual: " + response.asString());
}
```

Esto te mostrará si:
- ✓ El carrito es válido
- ✓ La sesión se mantiene
- ✓ El servidor responde correctamente

---

## 🛠️ RESUMEN DE CAMBIOS

| Archivo | Cambio | Razón |
|---------|--------|-------|
| `pom.xml` | Agregué `slf4j-simple` | Eliminar warning de SLF4J |
| `pom.xml` | Cambié Java `17` → `21` | Compatibilidad con tu JDK |
| `CheckoutHappyPathTest.java` | Agregué validaciones | Detectar valores nulos temprano |
| `CheckoutHappyPathTest.java` | Agregué debug logs | Ver exactamente qué se envía |
| `CheckoutHappyPathTest.java` | Agregué `.log().ifError()` | Ver respuestas de error |

---

## 📞 SI TODAVÍA HAY ERROR 404

Comparte conmigo:

1. ✉️ Los logs completos de la ejecución (con los mensajes DEBUG)
2. 🔗 La URL completa que construye (debe aparecer en los logs)
3. 📝 La respuesta de error del servidor (`.log().all()` te la mostrará)
4. ✅ Confirmación de que el endpoint existe en tu Swagger

---

**¡Ahora ejecuta los tests y comparte los resultados! 🚀**

