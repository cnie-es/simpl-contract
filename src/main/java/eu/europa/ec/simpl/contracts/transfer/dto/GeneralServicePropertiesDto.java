package eu.europa.ec.simpl.contracts.transfer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeneralServicePropertiesDto(

        @JsonProperty("simpl:name")
        String name,

        @JsonProperty("simpl:description")
        String description,

        @JsonProperty("simpl:offeringType")
        String offeringType

) {}
