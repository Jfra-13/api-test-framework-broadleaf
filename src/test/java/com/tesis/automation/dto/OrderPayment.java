package com.tesis.automation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para Pago de Orden
 * Contrato Base OE1: Estructura real de Broadleaf OrderPayment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPayment {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("orderId")
    private Long orderId;

    @JsonProperty("type")
    private String type; // CREDIT_CARD, etc.

    @JsonProperty("gatewayType")
    private String gatewayType; // NULL_GATEWAY para tests

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("billingAddress")
    private Address billingAddress;

    @JsonProperty("transactions")
    private List<PaymentTransaction> transactions;

    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @JsonProperty("active")
    private Boolean active;
}

