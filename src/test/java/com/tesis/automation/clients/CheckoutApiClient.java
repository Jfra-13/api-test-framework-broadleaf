package com.tesis.automation.clients;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cliente específico para operaciones de Checkout y Pago
 * OE2 - API Client Pattern
 */
public class CheckoutApiClient extends BaseApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutApiClient.class);

    /**
     * Añade información de pago a la orden
     * POST /cart/checkout/payment?cartId={cartId}
     */
    public Response addPaymentToOrder(String cartId, Object paymentPayload) {
        logger.info("Adding payment to order for cart: {}", cartId);
        Response response = postWithQuery("/cart/checkout/payment", "cartId", cartId, paymentPayload);
        logResponse(response);
        return response;
    }

    /**
     * Obtiene información del carrito/orden
     * GET /cart/{cartId}
     */
    public Response getOrderDetails(String cartId) {
        logger.info("Getting order details for cart: {}", cartId);
        String endpoint = String.format("/cart/%s", cartId);
        Response response = get(endpoint);
        logResponse(response);
        return response;
    }
}

