package com.tesis.automation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para Transacción de Pago
 * Contrato Base OE1: Estructura real de Broadleaf PaymentTransaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("type")
    private String type; // AUTHORIZE_AND_CAPTURE

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("additionalFields")
    private List<AdditionalField> additionalFields;

    @JsonProperty("parentTransactionId")
    private Long parentTransactionId;

    @JsonProperty("customerIpAddress")
    private String customerIpAddress;

    @JsonProperty("rawResponse")
    private String rawResponse;

    @JsonProperty("archived")
    private String archived;
}

