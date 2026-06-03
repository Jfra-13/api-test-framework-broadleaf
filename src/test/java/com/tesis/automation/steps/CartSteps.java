package com.tesis.automation.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import com.tesis.automation.clients.CartApiClient;
import com.tesis.automation.utils.DataGenerator;
import com.tesis.automation.utils.ScenarioContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CartSteps {

    private final CartApiClient cartApiClient = new CartApiClient();

    @Given("el usuario obtiene un nuevo carrito de compras")
    @Given("el usuario obtiene un nuevo carrito")
    public void userGetsNewCart() {
        Response response = cartApiClient.getOrCreateCart();
        assertThat(response.getStatusCode(), is(200));

        String cartId = response.jsonPath().getString("id");
        ScenarioContext.set(ScenarioContext.Keys.CART_ID, cartId);
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);

        System.out.println("Carrito creado con ID: " + cartId);
    }

    @When("el usuario agrega {int} unidad del SKU al carrito")
    public void userAddsItemToCart(int quantity) {
        String cartId = ScenarioContext.getString(ScenarioContext.Keys.CART_ID);
        String skuId  = ScenarioContext.getString(ScenarioContext.Keys.SKU_ID);

        String itemPayload = DataGenerator.generateAddItemPayload(Long.parseLong(skuId), quantity);
        Response response = cartApiClient.addItemToCart(cartId, itemPayload);
        assertThat(response.getStatusCode(), is(200));

        String fulfillmentGroupId = response.jsonPath().getString("fulfillmentGroups[0].id");
        ScenarioContext.set(ScenarioContext.Keys.FULFILLMENT_GROUP_ID, fulfillmentGroupId);
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);

        System.out.println("Item agregado. Fulfillment Group: " + fulfillmentGroupId);
    }

    @Then("el carrito debe tener {int} item")
    public void validateItemCount(int expected) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().getInt("itemCount"), is(expected));
    }

    @Then("el campo {string} debe existir en el carrito")
    public void fieldExistsInCart(String field) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().get(field), is(notNullValue()));
    }

    @Then("se captura el ID del carrito para usar en próximas solicitudes")
    @Then("se captura el cartId para usar en próximas solicitudes")
    public void captureCartId() {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        String cartId = response.jsonPath().getString("id");
        ScenarioContext.set(ScenarioContext.Keys.CART_ID, cartId);
        System.out.println("Cart ID capturado: " + cartId);
    }

    @Then("se captura el {string}")
    public void captureJsonPath(String jsonPath) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        String value = response.jsonPath().getString(jsonPath);
        if (jsonPath.contains("fulfillmentGroups")) {
            ScenarioContext.set(ScenarioContext.Keys.FULFILLMENT_GROUP_ID, value);
            System.out.println("Fulfillment Group ID capturado: " + value);
        } else {
            ScenarioContext.set(ScenarioContext.Keys.CART_ID, value);
            System.out.println("Valor capturado: " + value);
        }
    }
}
