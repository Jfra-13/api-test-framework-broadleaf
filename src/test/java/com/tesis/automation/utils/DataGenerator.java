package com.tesis.automation.utils;

import com.github.javafaker.Faker;
import com.tesis.automation.dto.Address;
import com.tesis.automation.dto.AdditionalField;
import com.tesis.automation.dto.IsoCountry;
import com.tesis.automation.dto.OrderPayment;
import com.tesis.automation.dto.PaymentTransaction;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Generador de datos de prueba usando Java Faker
 * OE2 - Eliminación de Hardcoding
 */
public class DataGenerator {

    private static final Faker faker = new Faker(new Locale("es-MX"));

    /**
     * Genera una dirección válida para pruebas
     */
    public static Address generateAddress() {
        return Address.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .addressLine1(faker.address().streetAddress())
                .city(faker.address().city())
                .stateProvinceRegion("TX") // Estado fijo para tests
                .postalCode("75001") // Código postal fijo para tests
                .isoCountryAlpha2(IsoCountry.builder()
                        .alpha2("US")
                        .name("United States")
                        .build())
                .build();
    }

    /**
     * Genera un payload de item para agregar al carrito
     */
    public static String generateAddItemPayload(Long skuId, int quantity) {
        return String.format(
                "{\"quantity\": %d, \"skuId\": %d}",
                quantity, skuId
        );
    }

    /**
     * Genera un payload de pago con tarjeta de crédito
     */
    public static OrderPayment generateCreditCardPayment(Long cartId, Double amount, Address billingAddress) {

        PaymentTransaction transaction = PaymentTransaction.builder()
                .type("AUTHORIZE_AND_CAPTURE")
                .success(true)
                .amount(amount)
                .currency("USD")
                .additionalFields(Arrays.asList(
                        AdditionalField.builder().key("number").value("1111222233334444").build(),
                        AdditionalField.builder().key("expMonth").value("12").build(),
                        AdditionalField.builder().key("expYear").value("2028").build(),
                        AdditionalField.builder().key("cvv").value("123").build()
                ))
                .build();

        return OrderPayment.builder()
                .orderId(cartId)
                .type("CREDIT_CARD")
                .gatewayType("NULL_GATEWAY") // Gateway de prueba
                .amount(amount)
                .currency("USD")
                .billingAddress(billingAddress)
                .transactions(List.of(transaction))
                .build();
    }

    /**
     * Genera un email aleatorio único
     */
    public static String generateEmail() {
        return faker.internet().emailAddress();
    }

    /**
     * Genera un nombre de usuario único
     */
    public static String generateUsername() {
        return faker.name().username();
    }
}

