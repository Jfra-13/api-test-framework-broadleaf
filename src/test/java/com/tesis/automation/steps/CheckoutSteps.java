package com.tesis.automation.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import com.tesis.automation.clients.CartApiClient;
import com.tesis.automation.dto.Address;
import com.tesis.automation.utils.DataGenerator;
import com.tesis.automation.utils.ScenarioContext;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CheckoutSteps {

    private final CartApiClient cartApiClient = new CartApiClient();

    @When("el usuario configura la dirección de envío")
    public void userConfiguresShippingAddress() {
        String cartId = ScenarioContext.getString(ScenarioContext.Keys.CART_ID);
        String fulfillmentGroupId = ScenarioContext.getString(ScenarioContext.Keys.FULFILLMENT_GROUP_ID);

        Address address = DataGenerator.generateAddress();
        ScenarioContext.set(ScenarioContext.Keys.BILLING_ADDRESS, address);

        Response response = cartApiClient.setShippingAddress(cartId, fulfillmentGroupId, address);
        assertThat(response.getStatusCode(), is(200));
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);

        System.out.println("Direccion de envio configurada");
    }

    @Then("la dirección debe estar registrada correctamente")
    @Then("la dirección quedó registrada correctamente")
    public void validateShippingAddressSet() {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().get("id"), is(notNullValue()));
    }

    @When("el usuario realiza el checkout")
    @When("el usuario realiza el checkout final")
    public void userPerformsCheckout() {
        String cartId = ScenarioContext.getString(ScenarioContext.Keys.CART_ID);
        Response response = cartApiClient.performCheckout(cartId);
        assertThat(response.getStatusCode(), anyOf(is(200), is(201)));
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);

        System.out.println("Checkout completado");
    }

    @Then("el status debe ser {string}")
    public void validateOrderStatus(String expected) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().getString("status"), is(expected));
    }

    @Then("debe existir un orderNumber")
    public void validateOrderNumber() {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().getString("orderNumber"), is(not(emptyOrNullString())));
        System.out.println("Orden confirmada: " + response.jsonPath().getString("orderNumber"));
    }

    @Then("la orden ha sido procesada exitosamente")
    public void validateOrderProcessed() {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().getString("status"), is("SUBMITTED"));
    }

    @Given("el usuario intenta agregar un producto inválido")
    public void userTriesToAddInvalidProduct() {
        // Setup — la accion real ocurre en el step POST con skuId invalido
    }

    @Then("la respuesta cumple con el contrato {string}")
    public void validateAgainstSchema(String schemaFile) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("schemas/" + schemaFile));
        System.out.println("Validacion de contrato exitosa: " + schemaFile);
    }
}
