# Objetivo Específico 2 (OE2): Arquitectura del Framework BDD

## 🏗️ Arquitectura Multicapa

```
┌─────────────────────────────────────────────────────────┐
│  FEATURES (Gherkin)                                     │
│  - checkout.feature                                     │
│  - product-catalog.feature                              │
│  - cart.feature                                         │
│  [Lenguaje de negocio, NO código]                       │
└──────────────┬──────────────────────────────────────────┘
               │
┌──────────────▼──────────────────────────────────────────┐
│  STEPS (Step Definitions - Cucumber)                   │
│  - CartSteps.java                                       │
│  - CatalogSteps.java                                    │
│  - CheckoutSteps.java                                   │
│  [Traduce Gherkin a Java]                               │
└──────────────┬──────────────────────────────────────────┘
               │
┌──────────────▼──────────────────────────────────────────┐
│  API CLIENTS (RestAssured Pattern)                      │
│  - CartApiClient.java                                   │
│  - CatalogApiClient.java                                │
│  - CheckoutApiClient.java                               │
│  [Desacopla HTTP del test]                              │
└──────────────┬──────────────────────────────────────────┘
               │
┌──────────────▼──────────────────────────────────────────┐
│  DTOs & UTILS                                           │
│  - Address.java, OrderPayment.java (Modelos)           │
│  - DataGenerator.java (Faker - Sin hardcoding)          │
│  - ScenarioContext.java (Compartir data)                │
│  - JsonSchemaValidator.java (Contract Testing)          │
└──────────────┬──────────────────────────────────────────┘
               │
┌──────────────▼──────────────────────────────────────────┐
│  REST ASSURED + HTTP                                    │
│  [Capa de transporte]                                   │
└─────────────────────────────────────────────────────────┘
```

---

## 📁 Estructura de Carpetas Recomendada

```
framework-automatizacion-api/
├── pom.xml           # Dependencias Maven
├── src/test/
│   ├── java/com/tesis/automation/
│   │   ├── clients/
│   │   │   ├── BaseApiClient.java
│   │   │   ├── CartApiClient.java
│   │   │   ├── CatalogApiClient.java
│   │   │   └── CheckoutApiClient.java
│   │   │
│   │   ├── dto/
│   │   │   ├── Address.java
│   │   │   ├── IsoCountry.java
│   │   │   ├── AdditionalField.java
│   │   │   ├── PaymentTransaction.java
│   │   │   ├── OrderPayment.java
│   │   │   └── OrderWrapper.java
│   │   │
│   │   ├── utils/
│   │   │   ├── DataGenerator.java
│   │   │   ├── ScenarioContext.java
│   │   │   └── JsonSchemaValidator.java
│   │   │
│   │   ├── steps/
│   │   │   ├── CartSteps.java
│   │   │   ├── CatalogSteps.java
│   │   │   ├── CheckoutSteps.java
│   │   │   └── PaymentSteps.java
│   │   │
│   │   ├── hooks/
│   │   │   └── CommonHooks.java
│   │   │
│   │   └── runners/
│   │       └── RunCucumberTest.java
│   │
│   └── resources/
│       ├── features/
│       │   ├── checkout.feature
│       │   ├── product-catalog.feature
│       │   └── cart.feature
│       │
│       ├── schemas/
│       │   ├── cart-schema.json
│       │   ├── checkout-schema.json
│       │   ├── product-schema.json
│       │   └── order-payment-schema.json
│       │
│       ├── application.properties
│       └── logback.xml
│
└── docs/
    ├── API-BASELINE.md
    ├── ARCHITECTURE.md
    └── REFACTORING-GUIDE.md
```

---

## 🎯 Principios de Diseño (OE2)

### 1. **Separación de Responsabilidades**

❌ **ANTES (Monolítico)**
```java
public class CheckoutTest {
    @Test
    public void testCheckout() {
        // TODO El código hace TODO:
        // - HTTP Requests
        // - Validaciones
        // TODO - Generación de datos
        // - Contexto compartido
    }
}
```

✅ **DESPUÉS (Multicapa)**
```
checkout.feature (Gherkin)
    ↓
CheckoutSteps.java (Lógica del test)
    ↓
CheckoutApiClient.java (HTTP)
    ↓
DataGenerator.java (Datos)
    ↓
ScenarioContext.java (Compartir data)
```

### 2. **Eliminación de Hardcoding**

❌ **ANTES**
```java
String requestBody = "{\n" +
    "  \"firstName\": \"Automatizacion\",\n" +
    "  \"lastName\": \"Test\",\n" +
    "  ...\n" +
"}";
```

✅ **DESPUÉS**
```java
Address address = DataGenerator.generateAddress();
OrderPayment payment = DataGenerator
    .generateCreditCardPayment(cartId, 3.99, address);
```

### 3. **Contract Testing**

✅ **Validación de Esquemas**
```java
response.then()
    .assertThat()
    .body(matchesJsonSchemaInClasspath("schemas/checkout-schema.json"));
```

---

## 📝 Ejemplo de Step Definition Refactorizado

### ANTES (CheckoutHappyPathTest.java - Monolítico)

```java
@Test
public void test_06_PerformCheckout() {
    Response response = given()
            .filter(sessionFilter)
            .header("Content-Type", "application/json")
            .queryParam("cartId", capturedCartId)
            .when()
            .post("/cart/checkout")
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .extract().response();
}
```

### DESPUÉS (CheckoutSteps.java - BDD)

```java
@When("el usuario realiza el checkout final")
public void userPerformsCheckout() {
    String cartId = ScenarioContext.getString(Keys.CART_ID);
    
    Response response = checkoutApiClient.performCheckout(cartId);
    
    ScenarioContext.set(Keys.LAST_RESPONSE, response);
}

@Then("la respuesta debe tener código de estado {int} o {int}")
public void validateStatusCode(int code1, int code2) {
    Response response = (Response) ScenarioContext.get(Keys.LAST_RESPONSE);
    
    assertThat(response.getStatusCode())
            .isIn(code1, code2);
}

@Then("el campo \"status\" debe ser \"SUBMITTED\"")
public void validateOrderStatus() {
    Response response = (Response) ScenarioContext.get(Keys.LAST_RESPONSE);
    
    assertThat(response.jsonPath().getString("status"))
            .isEqualTo("SUBMITTED");
}

@Then("la respuesta cumple con el contrato \"checkout-schema.json\"")
public void validateAgainstSchema() {
    Response response = (Response) ScenarioContext.get(Keys.LAST_RESPONSE);
    
    response.then()
            .assertThat()
            .body(matchesJsonSchemaInClasspath("schemas/checkout-schema.json"));
}
```

---

## 🚀 Ventajas de OE2

| Aspecto | Monolítico | BDD Multicapa |
|---------|-----------|---------------|
| **Reusabilidad** | ❌ Nula | ✅ Steps compartibles |
| **Mantenibilidad** | ❌ Difícil | ✅ Limpia |
| **Hardcoding** | ❌ Alto | ✅ DataGenerator |
| **Documentación** | ❌ Implícita | ✅ Gherkin es docs |
| **Contract Testing** | ❌ Manual | ✅ JSON Schemas |
| **Reportes** | ❌ Básicos | ✅ Allure |
| **Escalabilidad** | ❌ Limitada | ✅ N Scenarios |

---

## 📚 Librerías Clave

| Librería | Propósito | OE2 |
|----------|-----------|-----|
| **Cucumber** | Framework BDD | Gherkin→Java |
| **RestAssured** | HTTP Client | API Requests |
| **json-schema-validator** | Contract Testing | Validar esquemas |
| **Lombok** | Reduce Boilerplate | DTOs compactos |
| **JavaFaker** | Data Generation | Sin hardcoding |
| **Jackson** | JSON Serialization | DTO↔JSON |
| **Allure** | Test Reports | Reportes visuales |

---

## ✅ Checklist Final OE2

- [ ] **Features (Gherkin)** escriben el flujo en lenguaje de negocio
- [ ] **Steps** traducen Gherkin a automatización Java
- [ ] **API Clients** encapsulan RestAssured
- [ ] **DTOs** con Lombok reemplazan hardcoding
- [ ] **DataGenerator** genera datos aleatorios con Faker
- [ ] **ScenarioContext** comparte data entre steps
- [ ] **JSON Schemas** validan contratos
- [ ] **Hooks** limpian contexto en cada escenario
- [ ] **Test Runner** ejecuta Cucumber desde ClassPath
- [ ] **Allure Reports** generan reportes visuales

---

## 📖 Próximos Pasos

1. **Crear Test Runner**
   ```java
   @Suite
   @SelectClasspathResource("features/")
   public class RunCucumberTest {}
   ```

2. **Implementar Hooks**
   ```java
   @Before
   public void setup() { ScenarioContext.clear(); }
   
   @After
   public void teardown() { ScenarioContext.clear(); }
   ```

3. **Generar Reportes Allure**
   ```bash
   mvn clean test
   mvn allure:serve
   ```

4. **Documentar Hallazgos en Tesis**
   - Capítulo OE1: Contrato de APIs (API-BASELINE.md)
   - Capítulo OE2: Arquitectura (ARCHITECTURE.md)
   - Anexos: JSON Schemas + Feature Files

---

## 💡 Conclusión

**OE1** proporciona la **base técnica** (contratos de API documentados).
**OE2** proporciona la **estructura de pruebas** (BDD multicapa escalable).

Juntos crean un framework de **testing sostenible, reutilizable y alineado con la tesis**.

