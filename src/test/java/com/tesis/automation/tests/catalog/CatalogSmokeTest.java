package com.tesis.automation.tests.catalog;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CatalogSmokeTest {

    private static String capturedProductId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "https://localhost:7445/api/v1";
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    @Order(1)
    @DisplayName("Busca productos y captura dinámicamente el ID del primero.")
    void test_C1_SearchAndCaptureProduct() {
        Response response = given()
                .queryParam("q", "hot")
                .when()
                .get("/catalog/search")
                .then()
                .statusCode(200)
                .extract().response();

        capturedProductId = response.jsonPath().getString("products[0].id");

        System.out.println("ID de Producto capturado dinámicamente: " + capturedProductId);
    }

    @Test
    @Order(2)
    @DisplayName("Obtiene detalles de un producto y valida el contrato.")
    void test_C2_GetProductDetailsAndValidateSchema() {
        given()
                .pathParam("productId", capturedProductId)
                .when()
                .get("/catalog/product/{productId}")
                .then()
                .statusCode(200)
                .body("id", equalTo(Integer.parseInt(capturedProductId)))
                .body(matchesJsonSchemaInClasspath("schemas/contrato_detalle_producto.json"));
    }
}