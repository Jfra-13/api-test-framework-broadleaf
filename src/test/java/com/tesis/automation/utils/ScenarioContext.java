package com.tesis.automation.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Contexto compartido entre Step Definitions en un escenario Cucumber
 * OE2 - Patrón de comunicación entre steps
 */
public class ScenarioContext {

    private static final ThreadLocal<Map<String, Object>> contextMap =
            ThreadLocal.withInitial(HashMap::new);

    /**
     * Almacena un valor en el contexto
     */
    public static void set(String key, Object value) {
        contextMap.get().put(key, value);
    }

    /**
     * Recupera un valor del contexto
     */
    public static Object get(String key) {
        return contextMap.get().get(key);
    }

    /**
     * Recupera un valor como String
     */
    public static String getString(String key) {
        Object value = contextMap.get().get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Recupera un valor como Long
     */
    public static Long getLong(String key) {
        Object value = contextMap.get().get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    /**
     * Verifica si una clave existe en el contexto
     */
    public static boolean exists(String key) {
        return contextMap.get().containsKey(key);
    }

    /**
     * Limpia el contexto (importante al final de cada escenario)
     */
    public static void clear() {
        contextMap.get().clear();
    }

    /**
     * Keys constantes para el contexto
     */
    public static class Keys {
        public static final String CART_ID = "cartId";
        public static final String SKU_ID = "skuId";
        public static final String FULFILLMENT_GROUP_ID = "fulfillmentGroupId";
        public static final String ORDER_ITEM_ID = "orderItemId";
        public static final String SESSION_FILTER = "sessionFilter";
        public static final String LAST_RESPONSE = "lastResponse";
        public static final String BILLING_ADDRESS = "billingAddress";
    }
}

