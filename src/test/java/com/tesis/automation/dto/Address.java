package com.tesis.automation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una Dirección de Envío/Facturación
 * Contrato Base OE1: Mapping contra respuesta real de Broadleaf
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("addressLine1")
    private String addressLine1;

    @JsonProperty("addressLine2")
    private String addressLine2;

    @JsonProperty("city")
    private String city;

    @JsonProperty("stateProvinceRegion")
    private String stateProvinceRegion;

    @JsonProperty("postalCode")
    private String postalCode;

    @JsonProperty("isoCountryAlpha2")
    private IsoCountry isoCountryAlpha2;

    @JsonProperty("phonePrimary")
    private String phonePrimary;
}


