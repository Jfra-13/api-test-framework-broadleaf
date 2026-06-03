package com.tesis.automation.clients;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cliente específico para operaciones del Catálogo
 * OE2 - API Client Pattern
 */
public class CatalogApiClient extends BaseApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CatalogApiClient.class);

    /**
     * Busca productos por término
     * GET /catalog/search?q={searchTerm}
     */
    public Response searchProducts(String searchTerm) {
        logger.info("Searching products with term: {}", searchTerm);
        Response response = get("/catalog/search", "q", searchTerm);
        logResponse(response);
        return response;
    }

    /**
     * Obtiene los detalles de un producto específico
     * GET /catalog/product/{productId}
     */
    public Response getProductDetails(String productId) {
        logger.info("Getting product details for ID: {}", productId);
        String endpoint = String.format("/catalog/product/%s", productId);
        Response response = get(endpoint);
        logResponse(response);
        return response;
    }

    /**
     * Obtiene el catálogo de categorías
     * GET /catalog/categories
     */
    public Response getCategories() {
        logger.info("Getting product categories...");
        Response response = get("/catalog/categories");
        logResponse(response);
        return response;
    }
}

