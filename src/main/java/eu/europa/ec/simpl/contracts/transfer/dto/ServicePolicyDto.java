package eu.europa.ec.simpl.contracts.transfer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ServicePolicyDto(

        @JsonProperty("simpl:usage-policy")
        String usagePolicy,

        @JsonProperty("simpl:access-policy")
        String accessPolicy

) {}

