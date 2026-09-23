package eu.europa.ec.simpl.contracts.transfer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProviderInformationDto(

        @JsonProperty("simpl:providedBy")
        String providedBy,

        @JsonProperty("simpl:contact")
        String contact

) {}
