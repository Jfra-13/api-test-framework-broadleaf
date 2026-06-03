package com.tesis.automation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Campo Adicional (Tarjeta de Crédito)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdditionalField {

    @JsonProperty("key")
    private String key; // "number", "expMonth", "expYear", "cvv"

    @JsonProperty("value")
    private String value;
}

