package com.tesis.automation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para País ISO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IsoCountry {

    @JsonProperty("alpha2")
    private String alpha2;

    @JsonProperty("name")
    private String name;
}

