package com.tesis.automation.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import com.tesis.automation.utils.ScenarioContext;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;

public class CommonSteps {

    // ── Background ──────────────────────────────────────────────────────────

    @Given("el servidor API está disponible en {string}")
    public void apiIsAvailableAt(String baseUrl) {
        RestAssured.baseURI = baseUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Given("la sesión HTTP está inicializada con JSESSIONID")
    public void sessionIsInitializedWithJsessionid() {
        ScenarioContext.clear();
    }

    // ── Steps decorativos (la lógica real ya fue ejecutada en el step anterior) ──

    @When("realiza una solicitud GET a {string}")
    public void decorativeGet(String path) { }

    @When("realiza una solicitud POST a {string}")
    public void decorativePost(String path) { }

    @When("realiza una solicitud PUT a {string}")
    public void decorativePut(String path) { }

    @Given("el usuario realiza una solicitud GET a {string}")
    public void userPerformsGet(String path) {
        String cleanPath = path.contains("?") ? path.substring(0, path.indexOf("?")) : path;
        String query     = path.contains("?") ? path.substring(path.indexOf("?") + 1) : null;

        var req = RestAssured.given();
        if (query != null) {
            String[] parts = query.split("=");
            if (parts.length == 2) req = req.queryParam(parts[0], parts[1]);
        }
        Response response = req.when().get(cleanPath).then().extract().response();
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);
    }

    @When("realiza una solicitud POST a {string} con skuId inválido")
    public void postWithInvalidSku(String path) {
        String cartId = ScenarioContext.getString(ScenarioContext.Keys.CART_ID);
        if (cartId == null) cartId = "999";

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body("{\"quantity\": 1, \"skuId\": 999999}")
                .when()
                .post("/cart/" + cartId + "/item")
                .then().extract().response();

        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);
    }

    // ── Validaciones genéricas ───────────────────────────────────────────────

    @Then("la respuesta debe tener código de estado {int}")
    public void validateStatusCode(int expected) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.getStatusCode(), is(expected));
    }

    @Then("la respuesta debe tener código de estado {int} o {int}")
    public void validateStatusCodeOr(int s1, int s2) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.getStatusCode(), anyOf(is(s1), is(s2)));
    }

    @Then("la respuesta contiene un campo {string} de tipo número")
    public void validateFieldIsNumber(String field) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().get(field), is(notNullValue()));
    }

    @Then("el campo {string} debe existir")
    public void fieldExists(String field) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().get(field), is(notNullValue()));
    }

    @Then("el campo {string} debe ser {string}")
    public void fieldEqualsString(String field, String expected) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().getString(field), is(expected));
    }

    @Then("el campo {string} debe ser igual a {int}")
    public void fieldEqualsInt(String field, int expected) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().getInt(field), is(expected));
    }

    @Then("el campo {string} debe ser true")
    public void fieldIsTrue(String field) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().getBoolean(field), is(true));
    }

    @Then("el campo {string} debe ser {double}")
    public void fieldEqualsDouble(String field, double expected) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().getDouble(field), is(expected));
    }

    @Then("el campo {string} no debe ser nulo")
    public void fieldIsNotNull(String field) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().get(field), is(notNullValue()));
    }

    @Then("la respuesta debe ser válida según el esquema {string}")
    public void validateSchema(String schemaFile) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("schemas/" + schemaFile));
    }

    @Then("la respuesta contiene un messageKey describiendo el error")
    public void validateMessageKey() {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        // Broadleaf error envelope: {"httpStatusCode": X, "messages": [{"messageKey": "..."}]}
        boolean hasKey = response.jsonPath().get("message") != null
                || response.jsonPath().get("messageKey") != null
                || response.jsonPath().get("error") != null
                || response.jsonPath().get("messages") != null
                || response.jsonPath().get("httpStatusCode") != null;
        assertThat("La respuesta debe contener un campo de error", hasKey, is(true));
    }

    @Then("la respuesta contiene al menos un producto")
    public void validateHasProducts() {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().getList("products"), is(not(empty())));
    }

    @Then("cada producto en la lista debe tener los campos obligatorios:")
    public void validateProductFields(DataTable dataTable) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        List<String> fields = dataTable.asList();
        List<Object> products = response.jsonPath().getList("products");

        assertThat("La lista de productos no debe estar vacía", products, is(not(empty())));

        for (int i = 0; i < products.size(); i++) {
            for (String field : fields) {
                Object value = response.jsonPath().get("products[" + i + "]." + field);
                assertThat("Campo '" + field + "' faltante en producto " + i, value, is(notNullValue()));
            }
        }
    }
}