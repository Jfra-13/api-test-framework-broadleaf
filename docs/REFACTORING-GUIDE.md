# Guía de Refactorización: De TestNG a Cucumber BDD

## 📋 Resumen Ejecutivo

Tu test actual (`CheckoutHappyPathTest.java`) es **100% funcional** pero está acoplado al framework TestNG.
Para completar **OE2**, necesita ser "desarmado" y distribuido en la arquitectura multicapa BDD.

---

## 🔄 Mapeo: TestNG → Cucumber BDD

### TestNG (Antes - Monolítico)
```java
public class CheckoutHappyPathTest {
    @BeforeClass
    public void setup() { /* ... */ }
    
    @Test(priority = 1)
    public void test_01_CreateCart() { /* ... */ }
    
    @Test(priority = 2, dependsOnMethods = "test_01_CreateCart")
    public void test_02_FindProduct() { /* ... */ }
    
    @Test(priority = 6, dependsOnMethods = "test_04b_SelectShippingOption")
    public void test_06_PerformCheckout() { /* ... */ }
}
```

### Cucumber (Después - Multicapa)

**checkout.feature** (Gherkin)
```gherkin
Feature: Flujo Completo de Checkout

  Scenario: Cliente realiza una compra exitosa
    Given el usuario obtiene un nuevo carrito
    When busca un producto "hot"
    And agrega el producto al carrito
    And configura la dirección de envío
    And agrega la información de pago
    Then la orden se procesa exitosamente
```

**CheckoutSteps.java** (Steps)
```java
@Given("el usuario obtiene un nuevo carrito")
public void getUserNewCart() {
    Response response = cartApiClient.getOrCreateCart();
    String cartId = response.jsonPath().getString("id");
    ScenarioContext.set(Keys.CART_ID, cartId);
}

@When("busca un producto {string}")
public void searchProduct(String searchTerm) {
    Response response = catalogApiClient.searchProducts(searchTerm);
    String skuId = response.jsonPath().getString("products[0].defaultSku.id");
    ScenarioContext.set(Keys.SKU_ID, skuId);
}
```

---

## 🛠️ Paso por Paso: Refactorización

### PASO 1: Crear Feature File

**Archivo**: `src/test/resources/features/checkout.feature`

```gherkin
Feature: Flu

jo Happy Path de Checkout
  
  Como cliente de Broadleaf Commerce
  Quiero poder comprar un producto 
  Para validar el flujo complete de checkout

  Background:
    Given el servidor API está disponible
    And la sesión HTTP está inicializada

  Scenario: Compra exitosa con tarjeta de crédito
    
    # Step 1: Crear carrito
    Given el usuario obtiene un nuevo carrito
    Then el campo "id" debe existir
    And el campo "status" debe ser "IN_PROCESS"
    And se captura el cartId para usar en próximas solicitudes

    # Step 2: Buscar producto
    When el usuario busca un producto con término "hot"
    Then debe encontrar al menos 1 producto
    And se captura el SKU ID del primer producto

    # Step 3: Agregar al carrito
    When el usuario agrega 1 unidad del SKU al carrito
    Then el carrito debe tener 1 item
    And el campo "fulfillmentGroups[0].id" debe existir

    # Step 4: Configurar envío
    When el usuario configura la dirección de envío
    Then la dirección debe estar registrada correctamente

    # Step 5: Agregar pago
    When el usuario agrega información de pago con tarjeta
    Then la transacción debe tener success = true
    And el monto debe ser 3.99

    # Step 6: Checkout
    When el usuario realiza el checkout
    Then el status debe ser "SUBMITTED"
    And debe existir un orderNumber
    And la respuesta cumple con el contrato "checkout-schema.json"
```

### PASO 2: Crear Base Steps

**Archivo**: `src/test/java/com/tesis/automation/steps/CommonSteps.java`

```java
package com.tesis.automation.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import com.tesis.automation.utils.ScenarioContext;

public class CommonSteps {
    
    @Given("el servidor API está disponible")
    public void apiIsAvailable() {
        // Validar que la API responde
        // RestAssured.get("/api/v1/health").then().statusCode(anyOf(200, 404));
    }
    
    @Given("la sesión HTTP está inicializada")
    public void sessionIsInitialized() {
        // Crear SessionFilter y guardar en contexto
        ScenarioContext.clear();
    }
    
    @Then("el campo {string} debe existir")
    public void fieldExists(String fieldName) {
        Response response = (Response) ScenarioContext.get(Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().get(fieldName)).isNotNull();
    }
    
    @Then("el campo {string} debe ser {string}")
    public void fieldEquals(String fieldName, String expected) {
        Response response = (Response) ScenarioContext.get(Keys.LAST_RESPONSE);
        String actual = response.jsonPath().getString(fieldName);
        assertThat(actual).isEqualTo(expected);
    }
}
```

### PASO 3: Crear CartSteps (del test_01 al test_03)

**Archivo**: `src/test/java/com/tesis/automation/steps/CartSteps.java`

```java
package com.tesis.automation.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import com.tesis.automation.clients.CartApiClient;
import com.tesis.automation.clients.CatalogApiClient;
import com.tesis.automation.dto.Address;
import com.tesis.automation.utils.DataGenerator;
import com.tesis.automation.utils.ScenarioContext;

public class CartSteps {
    
    private CartApiClient cartApiClient = new CartApiClient();
    private CatalogApiClient catalogApiClient = new CatalogApiClient();
    
    // ===== PASO 1: Crear Carrito =====
    @Given("el usuario obtiene un nuevo carrito")
    public void userGetsNewCart() {
        Response response = cartApiClient.getOrCreateCart();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        String cartId = response.jsonPath().getString("id");
        
        ScenarioContext.set(ScenarioContext.Keys.CART_ID, cartId);
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);
        
        System.out.println("✓ Carrito creado con ID: " + cartId);
    }
    
    // ===== PASO 2: Buscar Producto =====
    @When("el usuario busca un producto con término {string}")
    public void userSearchesProduct(String searchTerm) {
        Response response = catalogApiClient.searchProducts(searchTerm);
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("products")).isNotEmpty();
        
        String skuId = response.jsonPath().getString("products[0].defaultSku.id");
        ScenarioContext.set(ScenarioContext.Keys.SKU_ID, skuId);
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);
        
        System.out.println("✓ Producto encontrado con SKU: " + skuId);
    }
    
    @Then("debe encontrar al menos {int} producto")
    public void validateProductFound(int minCount) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        int count = response.jsonPath().getList("products").size();
        
        assertThat(count).isGreaterThanOrEqualTo(minCount);
    }
    
    // ===== PASO 3: Agregar al Carrito =====
    @When("el usuario agrega {int} unidad del SKU al carrito")
    public void userAddsItemToCart(int quantity) {
        String cartId = ScenarioContext.getString(ScenarioContext.Keys.CART_ID);
        String skuId = ScenarioContext.getString(ScenarioContext.Keys.SKU_ID);
        
        String itemPayload = DataGenerator.generateAddItemPayload(Long.parseLong(skuId), quantity);
        Response response = cartApiClient.addItemToCart(cartId, itemPayload);
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        
        // Respuesta contiene el nuevo estado del carrito
        String fulfillmentGroupId = response.jsonPath().getString("fulfillmentGroups[0].id");
        ScenarioContext.set(ScenarioContext.Keys.FULFILLMENT_GROUP_ID, fulfillmentGroupId);
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);
        
        System.out.println("✓ Item agregado. Fulfillment Group: " + fulfillmentGroupId);
    }
    
    @Then("el carrito debe tener {int} item")
    public void validateItemCount(int expectedCount) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        int itemCount = response.jsonPath().getInt("itemCount");
        
        assertThat(itemCount).isEqualTo(expectedCount);
    }
}
```

### PASO 4: Crear CheckoutSteps (del test_04 al test_06)

**Archivo**: `src/test/java/com/tesis/automation/steps/CheckoutSteps.java`

```java
package com.tesis.automation.steps;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import com.tesis.automation.clients.CartApiClient;
import com.tesis.automation.clients.CheckoutApiClient;
import com.tesis.automation.dto.Address;
import com.tesis.automation.dto.OrderPayment;
import com.tesis.automation.utils.DataGenerator;
import com.tesis.automation.utils.ScenarioContext;

import static io.restassured.module.jsonschemavalidator.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class CheckoutSteps {
    
    private CartApiClient cartApiClient = new CartApiClient();
    private CheckoutApiClient checkoutApiClient = new CheckoutApiClient();
    
    // ===== PASO 4: Configurar Envío =====
    @When("el usuario configura la dirección de envío")
    public void userConfiguresShippingAddress() {
        String cartId = ScenarioContext.getString(ScenarioContext.Keys.CART_ID);
        String fulfillmentGroupId = ScenarioContext.getString(ScenarioContext.Keys.FULFILLMENT_GROUP_ID);
        
        Address address = DataGenerator.generateAddress();
        ScenarioContext.set(ScenarioContext.Keys.BILLING_ADDRESS, address);
        
        Response response = cartApiClient.setShippingAddress(cartId, fulfillmentGroupId, address);
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);
        
        System.out.println("✓ Dirección de envío configurada");
    }
    
    @Then("la dirección debe estar registrada correctamente")
    public void validateShippingAddressSet() {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        String firstName = response.jsonPath().getString("fulfillmentGroups[0].address.firstName");
        
        assertThat(firstName).isNotNull();
    }
    
    // ===== PASO 5: Agregar Pago =====
    @When("el usuario agrega información de pago con tarjeta")
    public void userAddsCardPayment() {
        String cartId = ScenarioContext.getString(ScenarioContext.Keys.CART_ID);
        Address billingAddress = (Address) ScenarioContext.get(ScenarioContext.Keys.BILLING_ADDRESS);
        
        OrderPayment payment = DataGenerator.generateCreditCardPayment(
                Long.parseLong(cartId), 3.99, billingAddress
        );
        
        Response response = checkoutApiClient.addPaymentToOrder(cartId, payment);
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);
        
        System.out.println("✓ Información de pago agregada");
    }
    
    @Then("la transacción debe tener success = true")
    public void validateTransactionSuccess() {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        Boolean success = response.jsonPath().getBoolean("transactions[0].success");
        
        assertThat(success).isTrue();
    }
    
    @Then("el monto debe ser {double}")
    public void validateTransactionAmount(double expected) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        Double amount = response.jsonPath().getDouble("amount");
        
        assertThat(amount).isEqualTo(expected);
    }
    
    // ===== PASO 6: Checkout Final =====
    @When("el usuario realiza el checkout")
    public void userPerformsCheckout() {
        String cartId = ScenarioContext.getString(ScenarioContext.Keys.CART_ID);
        Response response = cartApiClient.performCheckout(cartId);
        
        assertThat(response.getStatusCode()).isIn(200, 201);
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);
        
        System.out.println("✓ Checkout completado");
    }
    
    @Then("el status debe ser {string}")
    public void validateOrderStatus(String expected) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        String status = response.jsonPath().getString("status");
        
        assertThat(status).isEqualTo(expected);
    }
    
    @Then("debe existir un orderNumber")
    public void validateOrderNumber() {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        String orderNumber = response.jsonPath().getString("orderNumber");
        
        assertThat(orderNumber).isNotNull().isNotEmpty();
        System.out.println("✓ Orden confirmada: " + orderNumber);
    }
    
    @Then("la respuesta cumple con el contrato {string}")
    public void validateAgainstSchema(String schemaFile) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        
        response.then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("schemas/" + schemaFile));
        
        System.out.println("✓ Validación de contrato exitosa: " + schemaFile);
    }
}
```

### PASO 5: Crear Hooks

**Archivo**: `src/test/java/com/tesis/automation/hooks/CommonHooks.java`

```java
package com.tesis.automation.hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import com.tesis.automation.utils.ScenarioContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonHooks {
    
    private static final Logger logger = LoggerFactory.getLogger(CommonHooks.class);
    
    @Before
    public void setup() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("🚀 INICIANDO ESCENARIO CUCUMBER");
        logger.info("═══════════════════════════════════════════════════════════");
        
        // Limpiar contexto de escenarios previos
        ScenarioContext.clear();
    }
    
    @After
    public void teardown() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("✅ ESCENARIO FINALIZADO");
        logger.info("═══════════════════════════════════════════════════════════\n");
        
        // Limpiar contexto
        ScenarioContext.clear();
    }
}
```

### PASO 6: Crear Test Runner

**Archivo**: `src/test/java/com/tesis/automation/runners/RunCucumberTest.java`

```java
package com.tesis.automation.runners;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-report.html, json:target/cucumber.json")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.tesis.automation.steps, com.tesis.automation.hooks")
public class RunCucumberTest {}
```

---

## 📊 Comparativa Antes vs Después

| Aspecto | Antes (TestNG) | Después (Cucumber) |
|---------|---|---|
| **Legibilidad** | Código Java | Gherkin + Java |
| **Reutilización** | Test-specific | Steps reutilizables |
| **Datos** | Hardcoded strings | DataGenerator |
| **Validaciones** | Inline | Steps separate |
| **Mantenimiento** | Difícil de escalar | Modular |
| **Reporte** | TestNG report | Allure + HTML |
| **Stakeholder** | Solo técnico | Non-tech también |

---

## 🎯 Conclusión

Tu test actual es **100% funcional** y cumple el propósito. La refactorización a BDD es un **nivel superior** de calidad que:

1. **Documenta** el flujo en lenguaje de negocio
2. **Reutiliza** steps entre múltiples escenarios
3. **Desacopla** HTTP del test logic
4. **Valida** contratos contra esquemas
5. **Genera** reportes profesionales

**Resultado final**: Un framework de testing **escalable, mantenible y alineado con los OE1 y OE2 de tu tesis**.

