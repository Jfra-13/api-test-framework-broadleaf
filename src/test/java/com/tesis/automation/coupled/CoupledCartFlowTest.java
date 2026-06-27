package com.tesis.automation.coupled;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * OE4 — COUPLED baseline (counterfactual) for the cart contract flow.
 *
 * Deliberately coupled, schema-less mirror of the decoupled cart flow. It exists
 * only to measure Code Churn under the same five contract mutations (A, B, M2, M3,
 * SEM) and compare change propagation against the decoupled framework.
 *
 * Coupling on purpose — do NOT refactor this class:
 *   - RestAssured calls are inline in every test; there is no BaseApiClient / CartApiClient.
 *   - There is no JSON Schema: every contract field is asserted inline, field by field.
 *   - There is no DTO: the JSON is navigated with literal paths.
 *   - The contract field names ("id", "customer", "status", "itemCount") are repeated
 *     across scenarios, so a single contract change must be edited in every occurrence.
 *     That repetition is the whole point of the counterfactual.
 *
 * Note: the body is parsed from the raw string with JsonPath.from(...). The SUT mutation
 * filter rewrites the response body and drops the Content-Type header, which would make
 * the content-type-driven response.jsonPath() return null for every field. Parsing the
 * raw string keeps each mutation's real signal visible (and does not affect churn counts).
 *
 * Run in isolation (baseline must be GREEN before injecting any mutation):
 *   mvn -Dtest=CoupledCartFlowTest test
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoupledCartFlowTest {

    private static SessionFilter session;
    private static String cartId;
    private static String skuId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = resolveBaseUrl();
        RestAssured.useRelaxedHTTPSValidation();
        session = new SessionFilter();
    }

    // Same base-URL resolution as the decoupled framework, copied inline (no shared client).
    private static String resolveBaseUrl() {
        String url = System.getProperty("base.url");
        if (url == null || url.isBlank()) url = System.getenv("BASE_URL");
        if (url == null || url.isBlank()) url = "https://localhost:7445/api/v1";
        return url;
    }

    @Test
    @Order(1)
    @DisplayName("Coupled: create cart and validate the contract inline")
    void createCart_validatesContractInline() {
        Response r = given()
                .filter(session)
                .accept("application/json")
                .when()
                .get("/cart")
                .then()
                .statusCode(200)
                .extract().response();

        JsonPath json = JsonPath.from(r.asString());

        // Inline contract validation (no schema) — field by field.
        assertThat(json.get("id"), is(notNullValue()));
        assertThat(json.get("id"), is(instanceOf(Integer.class)));
        assertThat(json.get("customer"), is(notNullValue()));
        assertThat(json.get("status"), is(notNullValue()));
        assertThat(json.getInt("itemCount"), is(0));

        cartId = json.getString("id");
        System.out.println("[COUPLED] cart created, id=" + cartId);
    }

    @Test
    @Order(2)
    @DisplayName("Coupled: find a product and capture its SKU")
    void findProduct_capturesSku() {
        Response r = given()
                .filter(session)
                .accept("application/json")
                .queryParam("q", "hot")
                .when()
                .get("/catalog/search")
                .then()
                .statusCode(200)
                .extract().response();

        JsonPath json = JsonPath.from(r.asString());
        assertThat(json.getList("products"), is(not(empty())));

        skuId = json.getString("products[0].defaultSku.id");
        System.out.println("[COUPLED] sku=" + skuId);
    }

    @Test
    @Order(3)
    @DisplayName("Coupled: add item and re-validate the contract inline")
    void addItem_validatesContractInline() {
        String body = "{\"quantity\": 1, \"skuId\": " + skuId + "}";

        Response r = given()
                .filter(session)
                .accept("application/json")
                .header("Content-Type", "application/json")
                .pathParam("cartId", cartId)
                .body(body)
                .when()
                .post("/cart/{cartId}/item")
                .then()
                .statusCode(200)
                .extract().response();

        JsonPath json = JsonPath.from(r.asString());

        // No schema to reuse: the full contract is asserted inline again.
        assertThat(json.get("id"), is(notNullValue()));
        assertThat(json.get("customer"), is(notNullValue()));
        assertThat(json.get("status"), is(notNullValue()));
        assertThat(json.getInt("itemCount"), is(1));

        System.out.println("[COUPLED] item added, itemCount=1");
    }

    @Test
    @Order(4)
    @DisplayName("Coupled: re-fetch cart and validate the full contract inline")
    void refetchCart_validatesContractInline() {
        Response r = given()
                .filter(session)
                .accept("application/json")
                .when()
                .get("/cart")
                .then()
                .statusCode(200)
                .extract().response();

        JsonPath json = JsonPath.from(r.asString());

        // The price of having no schema: the same contract, re-checked field by field.
        assertThat(json.get("id"), is(notNullValue()));
        assertThat(json.get("id"), is(instanceOf(Integer.class)));
        assertThat(json.get("customer"), is(notNullValue()));
        assertThat(json.get("status"), is(notNullValue()));
        assertThat(json.getInt("itemCount"), is(1));

        cartId = json.getString("id");
        System.out.println("[COUPLED] cart re-fetched, contract re-validated inline");
    }
}
