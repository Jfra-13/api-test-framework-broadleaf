package com.tesis.automation.utils;

import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class JsonSchemaValidator {

    private static final String SCHEMAS_PATH = "schemas/";

    private JsonSchemaValidator() { }

    public static void validate(Response response, String schemaFile) {
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath(SCHEMAS_PATH + schemaFile));
    }

    public static void validateCart(Response response) {
        validate(response, "cart-schema.json");
    }

    public static void validateCheckout(Response response) {
        validate(response, "checkout-schema.json");
    }

    public static void validatePayment(Response response) {
        validate(response, "order-payment-schema.json");
    }

    public static void validateProductCatalog(Response response) {
        validate(response, "product-catalog-schema.json");
    }
}
