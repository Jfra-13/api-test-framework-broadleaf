package com.tesis.automation.clients;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

/**
 * Cliente Base para todas las llamadas HTTP
 *
 * Este patrón desacopla RestAssured de la lógica de pruebas
 * OE2 - Capa de Abstracción de API
 */
public abstract class BaseApiClient {

    private static final Logger logger = LoggerFactory.getLogger(BaseApiClient.class);

    protected static final String BASE_URL = resolveBaseUrl();
    protected SessionFilter sessionFilter;

    public BaseApiClient() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.useRelaxedHTTPSValidation();
        // En CI el SUT se expone con un túnel (ngrok). El plan free de ngrok muestra una
        // página de aviso a los clientes "tipo navegador"; este header la salta.
        // Solo se activa cuando la URL es de ngrok, así el modo local no se ve afectado.
        if (BASE_URL.contains("ngrok")) {
            RestAssured.requestSpecification = new io.restassured.builder.RequestSpecBuilder()
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .build();
        }
        this.sessionFilter = new SessionFilter();
    }

    /**
     * Resuelve la URL base del SUT con esta prioridad:
     *   1. Propiedad de sistema  -Dbase.url=...   (override manual en local)
     *   2. Variable de entorno   BASE_URL          (la inyecta el pipeline de CI)
     *   3. Valor por defecto: https://localhost:7445/api/v1 (Broadleaf en local)
     *
     * Esto permite ejecutar EXACTAMENTE el mismo framework en local y en la nube
     * sin tocar una sola línea de código (clave para OE3 y OE4).
     */
    private static String resolveBaseUrl() {
        String url = System.getProperty("base.url");
        if (url == null || url.isBlank()) {
            url = System.getenv("BASE_URL");
        }
        if (url == null || url.isBlank()) {
            url = "https://localhost:7445/api/v1";
        }
        logger.info("SUT base URL = {}", url);
        return url;
    }

    /**
     * Ejecuta una solicitud GET
     */
    protected Response get(String endpoint) {
        logger.info("GET {}/{}", BASE_URL, endpoint);
        return given()
                .filter(sessionFilter)
                .when()
                .get(endpoint)
                .andReturn();
    }

    /**
     * Ejecuta una solicitud GET con query parameters
     */
    protected Response get(String endpoint, String queryParam, String value) {
        logger.info("GET {}/{}?{}={}", BASE_URL, endpoint, queryParam, value);
        return given()
                .filter(sessionFilter)
                .queryParam(queryParam, value)
                .when()
                .get(endpoint)
                .andReturn();
    }

    /**
     * Ejecuta una solicitud POST con body JSON
     */
    protected Response post(String endpoint, Object body) {
        logger.info("POST {}/{}", BASE_URL, endpoint);
        return given()
                .filter(sessionFilter)
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post(endpoint)
                .andReturn();
    }

    /**
     * Ejecuta una solicitud POST con query parameter
     */
    protected Response postWithQuery(String endpoint, String queryParam, String value, Object body) {
        logger.info("POST {}/{}?{}={}", BASE_URL, endpoint, queryParam, value);
        var req = given()
                .filter(sessionFilter)
                .header("Content-Type", "application/json")
                .queryParam(queryParam, value);
        if (body != null) {
            req = req.body(body);
        }
        return req.when().post(endpoint).andReturn();
    }

    /**
     * Ejecuta una solicitud PUT con body JSON
     */
    protected Response put(String endpoint, Object body) {
        logger.info("PUT {}/{}", BASE_URL, endpoint);
        return given()
                .filter(sessionFilter)
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .put(endpoint)
                .andReturn();
    }

    /**
     * Log de respuesta importante
     */
    protected void logResponse(Response response) {
        logger.info("Status: {} | Body Length: {}", response.getStatusCode(), response.getBody().asString().length());
    }
}

