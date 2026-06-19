/*
 * OE4 — Experimento de Inyección y Code Churn (TESIS QA)
 * ======================================================
 * Esta clase NO pertenece al framework de pruebas. Es el artefacto de
 * "inyección controlada" que se pega DENTRO del SUT (Broadleaf DemoSite),
 * en el módulo `api`, para simular un cambio de contrato del backend.
 *
 * UBICACIÓN DESTINO (copiar este archivo a):
 *   C:\software\projects\TESIS_QA\WEBSITE\DemoSite\api\src\main\java\
 *       com\community\api\endpoint\cart\Oe4CartContractMutationFilter.java
 *
 * QUÉ HACE:
 *   Intercepta las respuestas que el framework valida con cart-schema.json
 *   (GET /cart y POST /cart/{id}/item) y aplica UNA mutación de contrato a la
 *   vez, controlada por configuración. Es el equivalente observable a cambiar
 *   el contrato en el controller REST, sin tocar la lógica de negocio real.
 *
 * CÓMO SE CONTROLA:
 *   - ACTIVE   = true/false  -> activa o desactiva la mutación (reversible).
 *   - MUTATION = "A" | "B" | "M2" | "M3" | "SEM"  -> elige qué mutación aplicar:
 *        "A"   (solo-contrato)      : renombra "customer" -> "cliente"
 *        "B"   (funcional)          : renombra "id"       -> "idCarrito"
 *        "M2"  (cambio de tipo)     : "itemCount" entero  -> string  ("1" en vez de 1)
 *        "M3"  (quita llave req.)   : elimina la llave "status" del JSON
 *        "SEM" (semántica)          : "itemCount" mentiroso SOLO en GET /cart
 *                                     (JSON estructuralmente válido, valor falso)
 *
 * REVERTIR:
 *   Pon ACTIVE = false (o borra el archivo) y vuelve a levantar Broadleaf.
 *
 * Nota: Broadleaf 3.3.7 usa la API Servlet de Java EE (javax.servlet.*),
 *       igual que CustomCartEndpoint.java en este mismo paquete.
 */
package com.community.api.endpoint.cart;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Component
@Order(1)
public class Oe4CartContractMutationFilter implements Filter {

    /** Interruptor maestro. Pon en false para desactivar sin borrar la clase. */
    private static final boolean ACTIVE = true;

    /** Mutación activa: "A" | "B" | "M2" | "M3" | "SEM". Una a la vez. */
    private static final String MUTATION = "SEM";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // El cart-schema.json valida DOS respuestas: GET /cart y POST /cart/{id}/item.
        // Para que la mutación de contrato sea consistente, se aplica en ambas
        // (las respuestas de checkout/payment quedan intactas).
        boolean isGetCart = "GET".equalsIgnoreCase(method)
                && uri != null && (uri.endsWith("/cart") || uri.endsWith("/cart/"));
        boolean isAddItem = "POST".equalsIgnoreCase(method)
                && uri != null && uri.matches(".*/cart/[^/]+/item/?");
        boolean shouldMutate = isGetCart || isAddItem;

        // Si está inactivo o no es una respuesta validada por cart-schema, pasa de largo.
        if (!ACTIVE || !shouldMutate) {
            chain.doFilter(req, res);
            return;
        }

        // Capturamos el cuerpo de la respuesta para poder reescribirlo.
        CapturingResponseWrapper wrapper = new CapturingResponseWrapper(response);
        chain.doFilter(req, wrapper);

        String body = wrapper.getCapturedBody();

        if (body != null && !body.isEmpty()) {
            switch (MUTATION == null ? "" : MUTATION.toUpperCase()) {
                case "A":
                    // Solo-contrato: renombra la llave "customer" -> "cliente".
                    body = body.replaceFirst("\"customer\"\\s*:", "\"cliente\":");
                    break;
                case "B":
                    // Funcional: renombra la llave de primer nivel "id" -> "idCarrito".
                    body = body.replaceFirst("\\{\\s*\"id\"\\s*:", "{\"idCarrito\":");
                    break;
                case "M2":
                    // Cambio de tipo: "itemCount": 1  ->  "itemCount": "1" (entero -> string).
                    body = body.replaceFirst("(\"itemCount\"\\s*:\\s*)(\\d+)", "$1\"$2\"");
                    break;
                case "M3":
                    // Elimina la llave requerida "status" del JSON (rompe el contrato).
                    body = body.replaceFirst("\"status\"\\s*:\\s*\"[^\"]*\"\\s*,\\s*", "");
                    break;
                case "SEM":
                    // Semántica: SOLO en GET /cart, pone un itemCount mentiroso.
                    // El JSON sigue siendo estructuralmente válido (entero) => el schema PASA.
                    if (isGetCart) {
                        body = body.replaceAll("\"itemCount\"\\s*:\\s*\\d+", "\"itemCount\": 99");
                    }
                    break;
                default:
                    // Sin mutación reconocida: respuesta intacta.
                    break;
            }
        }

        byte[] out = (body == null) ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(out.length);
        response.getOutputStream().write(out);
        response.getOutputStream().flush();
    }

    /** HttpServletResponseWrapper que almacena el cuerpo en memoria en vez de enviarlo. */
    private static class CapturingResponseWrapper extends HttpServletResponseWrapper {

        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override public void write(int b) { buffer.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener listener) { /* no-op */ }
        };
        private final PrintWriter writer =
                new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));

        CapturingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() {
            return servletOutputStream;
        }

        @Override
        public PrintWriter getWriter() {
            return writer;
        }

        @Override
        public void flushBuffer() {
            writer.flush();
        }

        String getCapturedBody() {
            writer.flush();
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
