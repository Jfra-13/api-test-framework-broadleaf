package com.tesis.automation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderWrapper {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("orderNumber")
    private String orderNumber;

    @JsonProperty("status")
    private String status;

    @JsonProperty("emailAddress")
    private String emailAddress;

    @JsonProperty("itemCount")
    private Integer itemCount;

    @JsonProperty("subTotal")
    private Map<String, Object> subTotal;

    @JsonProperty("total")
    private Map<String, Object> total;

    @JsonProperty("orderItems")
    private List<Map<String, Object>> orderItems;

    @JsonProperty("fulfillmentGroups")
    private List<Map<String, Object>> fulfillmentGroups;

    @JsonProperty("payments")
    private List<OrderPayment> payments;
}
