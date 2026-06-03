package com.tesis.automation.clients;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cliente específico para operaciones del Carrito
 * OE2 - API Client Pattern
 */
public class CartApiClient extends BaseApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CartApiClient.class);

    /**
     * Obtiene o crea un carrito para el cliente actual
     * GET /cart
     */
    public Response getOrCreateCart() {
        logger.info("Creating new cart...");
        Response response = get("/cart");
        logResponse(response);
        return response;
    }

    /**
     * Agrega un item al carrito
     * POST /cart/{cartId}/item
     */
    public Response addItemToCart(String cartId, Object itemPayload) {
        logger.info("Adding item to cart: {}", cartId);
        String endpoint = String.format("/cart/%s/item", cartId);
        Response response = post(endpoint, itemPayload);
        logResponse(response);
        return response;
    }

    /**
     * Configura la dirección de envío
     * PUT /shipping/{cartId}/{fulfillmentGroupId}/address
     */
    public Response setShippingAddress(String cartId, String fulfillmentGroupId, Object addressPayload) {
        logger.info("Setting shipping address for cart: {} / fg: {}", cartId, fulfillmentGroupId);
        String endpoint = String.format("/shipping/%s/%s/address", cartId, fulfillmentGroupId);
        Response response = put(endpoint, addressPayload);
        logResponse(response);
        return response;
    }

    /**
     * Realiza el checkout final
     * POST /cart/checkout?cartId={cartId}
     */
    public Response performCheckout(String cartId) {
        logger.info("Performing checkout for cart: {}", cartId);
        Response response = postWithQuery("/cart/checkout", "cartId", cartId, null);
        logResponse(response);
        return response;
    }
}

