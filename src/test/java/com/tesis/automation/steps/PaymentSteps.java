package com.tesis.automation.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import com.tesis.automation.clients.CheckoutApiClient;
import com.tesis.automation.dto.Address;
import com.tesis.automation.dto.OrderPayment;
import com.tesis.automation.utils.DataGenerator;
import com.tesis.automation.utils.ScenarioContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PaymentSteps {

    private final CheckoutApiClient checkoutApiClient = new CheckoutApiClient();

    @When("el usuario agrega información de pago con tarjeta de crédito")
    @When("el usuario agrega información de pago con tarjeta")
    public void userAddsCardPayment() {
        String cartId = ScenarioContext.getString(ScenarioContext.Keys.CART_ID);
        Address billingAddress = (Address) ScenarioContext.get(ScenarioContext.Keys.BILLING_ADDRESS);

        OrderPayment payment = DataGenerator.generateCreditCardPayment(
                Long.parseLong(cartId), 3.99, billingAddress);

        Response response = checkoutApiClient.addPaymentToOrder(cartId, payment);
        assertThat(response.getStatusCode(), is(200));
        ScenarioContext.set(ScenarioContext.Keys.LAST_RESPONSE, response);

        System.out.println("Informacion de pago agregada");
    }

    @When("incluye billingAddress")
    public void includesBillingAddress() { }

    @When("incluye transacción con tipo {string}")
    public void includesTransactionType(String type) { }

    @Then("la transacción debe tener success = true")
    public void validateTransactionSuccess() {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().getBoolean("transactions[0].success"), is(true));
    }

    @Then("el monto debe ser {double}")
    public void validateTransactionAmount(double expected) {
        Response response = (Response) ScenarioContext.get(ScenarioContext.Keys.LAST_RESPONSE);
        assertThat(response.jsonPath().getDouble("amount"), is(expected));
    }
}
