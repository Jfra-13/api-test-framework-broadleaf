package com.tesis.automation.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import com.tesis.automation.clients.CatalogApiClient;
import com.tesis.automation.utils.ScenarioContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CatalogSteps {

    private final CatalogApiClient catalogApiClient = new CatalogApiClient();

    @When("el usuario busca un producto con término {string}")
    public void userSearchesProduct(String searchTerm) {
        Response response = catalogApiClient.searchProducts(searchTerm);
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().getList("products"), is(not(empty())));

        String skuId = response.jsonPath().getString("products[0].defaultSku.id");
        ScenarioContext.set(ScenarioContext.Keys.SKU_ID, skuId);
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);

        System.out.println("Producto encontrado con SKU: " + skuId);
    }

    @Then("debe encontrar al menos {int} producto")
    public void validateProductFound(int minCount) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().getList("products").size(), is(greaterThanOrEqualTo(minCount)));
    }

    @Then("se captura el primer SKU ID encontrado")
    @Then("se captura el SKU ID del primer producto")
    public void captureSkuId() {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        String skuId = response.jsonPath().getString("products[0].defaultSku.id");
        ScenarioContext.set(ScenarioContext.Keys.SKU_ID, skuId);
        System.out.println("SKU ID capturado: " + skuId);
    }

    @Given("el usuario obtiene los detalles del producto con ID {string}")
    public void userGetsProductDetails(String productId) {
        Response response = catalogApiClient.getProductDetails(productId);
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);
    }
}
