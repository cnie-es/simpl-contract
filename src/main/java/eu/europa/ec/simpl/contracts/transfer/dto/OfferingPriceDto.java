package eu.europa.ec.simpl.contracts.transfer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OfferingPriceDto(

        @JsonProperty("simpl:currency")
        String currency,

        @JsonProperty("simpl:price")
        PriceValueDto price

) {}

