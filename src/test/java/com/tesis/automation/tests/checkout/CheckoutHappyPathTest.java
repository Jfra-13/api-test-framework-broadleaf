package com.tesis.automation.tests.checkout;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CheckoutHappyPathTest {

    private static SessionFilter sessionFilter;

    private static String capturedCartId;
    private static String capturedSkuId;
    private static String capturedOrderItemId;
    private static String capturedFulfillmentGroupId;
    private static String capturedShippingOptionId;
    private static double capturedOrderTotal = 3.99;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "https://localhost:7445/api/v1";
        RestAssured.useRelaxedHTTPSValidation();
        sessionFilter = new SessionFilter();
    }

    @Test
    @Order(1)
    @DisplayName("Paso 1: Obtiene un carrito nuevo y guarda la sesión.")
    void test_01_CreateCartAndStartSession() {
        Response response = given()
                .filter(sessionFilter)
                .when()
                .get("/cart")
                .then()
                .statusCode(200)
                .extract().response();

        capturedCartId = response.jsonPath().getString("id");
        System.out.println("Carrito creado con ID: " + capturedCartId);
    }

    @Test
    @Order(2)
    @DisplayName("Paso 2: Busca un producto y obtiene su SKU.")
    void test_02_FindProduct() {
        Response response = given()
                .queryParam("q", "hot")
                .when()
                .get("/catalog/search")
                .then()
                .statusCode(200)
                .body("products", not(empty()))
                .extract().response();

        capturedSkuId = response.jsonPath().getString("products[0].defaultSku.id");
        System.out.println("SKU ID encontrado para comprar: " + capturedSkuId);
    }

    @Test
    @Order(3)
    @DisplayName("Paso 3: Añade el producto al carrito y captura los IDs.")
    void test_03_AddItemToCart() {
        String requestBody = "{\n" +
                "  \"quantity\": 1,\n" +
                "  \"skuId\": " + capturedSkuId + "\n" +
                "}";

        Response response = given()
                .filter(sessionFilter)
                .header("Content-Type", "application/json")
                .pathParam("cartId", capturedCartId)
                .body(requestBody)
                .when()
                .post("/cart/{cartId}/item")
                .then()
                .statusCode(200)
                .body("itemCount", equalTo(1))
                .extract().response();

        capturedOrderItemId = response.jsonPath().getString("orderItems[0].id");
        System.out.println("Item añadido al carrito. Order Item ID: " + capturedOrderItemId);

        capturedFulfillmentGroupId = response.jsonPath().getString("fulfillmentGroups[0].id");
        System.out.println("Grupo de Envío encontrado con ID: " + capturedFulfillmentGroupId);
    }

    @Test
    @Order(4)
    @DisplayName("Paso 4: Actualiza la dirección del grupo de envío existente.")
    void test_04_ConfigureShipping() {
        String requestBody = "{\n" +
                "  \"firstName\": \"Automatizacion\",\n" +
                "  \"lastName\": \"Test\",\n" +
                "  \"addressLine1\": \"Calle Automatizada 123\",\n" +
                "  \"city\": \"Tech City\",\n" +
                "  \"stateProvinceRegion\": \"TX\",\n" +
                "  \"postalCode\": \"75001\",\n" +
                "  \"isoCountryAlpha2\": {\n" +
                "    \"alpha2\": \"US\"\n" +
                "  }\n" +
                "}";

        given()
                .filter(sessionFilter)
                .header("Content-Type", "application/json")
                .pathParam("cartId", capturedCartId)
                .pathParam("fulfillmentGroupId", capturedFulfillmentGroupId)
                .body(requestBody)
                .when()
                .put("/shipping/{cartId}/{fulfillmentGroupId}/address")
                .then()
                .statusCode(200);

        System.out.println("Dirección de envío asignada correctamente al grupo: " + capturedFulfillmentGroupId);
    }

    @Test
    @Order(5)
    @DisplayName("Paso 4b: Obtiene opciones de envío y asigna una al grupo.")
    void test_04b_SelectShippingOption() {
        System.out.println("\n=== Configurando Fulfillment Option ===");
        System.out.println("CartId: " + capturedCartId + " | FulfillmentGroupId: " + capturedFulfillmentGroupId);

        // Paso 1: Obtener las opciones de envío — fulfillmentType es obligatorio en Broadleaf
        Response optionsResponse = given()
                .filter(sessionFilter)
                .queryParam("cartId", capturedCartId)
                .queryParam("fulfillmentGroupId", capturedFulfillmentGroupId)
                .queryParam("fulfillmentType", "SHIP")
                .when()
                .get("/shipping/options")
                .then()
                .log().all()
                .extract().response();

        System.out.println("Opciones disponibles: " + optionsResponse.getBody().asString());

        // Paso 2: Tomar el ID de la primera opción disponible
        capturedShippingOptionId = optionsResponse.jsonPath().getString("[0].id");

        if (capturedShippingOptionId == null) {
            System.out.println("⚠️ No hay opciones de envío disponibles, saltando asignación.");
            System.out.println("=== Fin Configuración ===\n");
            return;
        }

        System.out.println("Fulfillment Option ID encontrado: " + capturedShippingOptionId);

        // Paso 3: Asignar la opción al grupo de envío
        Response response = given()
                .filter(sessionFilter)
                .header("Content-Type", "application/json")
                .pathParam("cartId", capturedCartId)
                .pathParam("fulfillmentGroupId", capturedFulfillmentGroupId)
                .pathParam("fulfillmentOptionId", capturedShippingOptionId)
                .when()
                .put("/shipping/{cartId}/group/{fulfillmentGroupId}/option/{fulfillmentOptionId}")
                .then()
                .log().all()
                .extract().response();

        System.out.println("Fulfillment Option asignada. Status: " + response.getStatusCode());

        // Captura el total actualizado del grupo de envío (incluye costo de envío)
        Double groupTotal = response.jsonPath().getDouble("total.amount");
        if (groupTotal != null && groupTotal > 0) {
            capturedOrderTotal = groupTotal;
            System.out.println("Order total actualizado (con envio): " + capturedOrderTotal);
        }
        System.out.println("=== Fin Configuración ===\n");
    }

    @Test
    @Order(6)
    @DisplayName("Paso 5: Añade la información de pago simulada.")
    void test_05_AddPayment() {
        String requestBody = "{\n" +
                "  \"orderId\": " + capturedCartId + ",\n" +
                "  \"type\": \"CREDIT_CARD\",\n" +
                "  \"gatewayType\": \"NULL_GATEWAY\",\n" +
                "  \"amount\": " + capturedOrderTotal + ",\n" +
                "  \"billingAddress\": {\n" +
                "    \"firstName\": \"Automatizacion\",\n" +
                "    \"lastName\": \"Test\",\n" +
                "    \"addressLine1\": \"Calle Automatizada 123\",\n" +
                "    \"city\": \"Tech City\",\n" +
                "    \"stateProvinceRegion\": \"TX\",\n" +
                "    \"postalCode\": \"75001\",\n" +
                "    \"isoCountryAlpha2\": {\n" +
                "      \"alpha2\": \"US\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"transactions\": [\n" +
                "    {\n" +
                "      \"type\": \"AUTHORIZE_AND_CAPTURE\",\n" +
                "      \"success\": true,\n" +
                "      \"amount\": " + capturedOrderTotal + ",\n" +
                "      \"additionalFields\": [\n" +
                "        { \"key\": \"number\", \"value\": \"1111222233334444\" },\n" +
                "        { \"key\": \"expMonth\", \"value\": \"12\" },\n" +
                "        { \"key\": \"expYear\", \"value\": \"2028\" },\n" +
                "        { \"key\": \"cvv\", \"value\": \"123\" }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Response response = given()
                .log().all()
                .filter(sessionFilter)
                .header("Content-Type", "application/json")
                .queryParam("cartId", capturedCartId)
                .body(requestBody)
                .when()
                .post("/cart/checkout/payment")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response();

        System.out.println("\n=== Verificación de Pago ===");
        String success = response.jsonPath().getString("transactions[0].success");
        System.out.println("Transaction Success: " + success);
        if (success == null || success.equals("null")) {
            System.out.println("⚠️ ADVERTENCIA: Success es null! Esto causará error en checkout.");
        }

        System.out.println("Información de pago añadida.");
    }

    @Test
    @Order(7)
    @DisplayName("Paso 6: Finaliza el Checkout de la orden.")
    void test_06_PerformCheckout() {
        Response response = given()
                .filter(sessionFilter)
                .header("Content-Type", "application/json")
                .queryParam("cartId", capturedCartId)
                .when()
                .post("/cart/checkout")
                .then()
                .log().all()
                .statusCode(anyOf(equalTo(200), equalTo(201)))
                .extract().response();

        System.out.println("¡Checkout Completado con Éxito!");
        System.out.println("Orden: " + response.jsonPath().getString("orderNumber"));
    }
}