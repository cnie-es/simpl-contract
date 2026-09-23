package eu.europa.ec.simpl.contracts.transfer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CredentialSubjectDto(

        @JsonProperty("@id")
        String id,

        @JsonProperty("simpl:providerInformation")
        ProviderInformationDto providerInformation,

        @JsonProperty("simpl:generalServiceProperties")
        GeneralServicePropertiesDto generalServiceProperties,

        @JsonProperty("simpl:offeringPrice")
        OfferingPriceDto offeringPrice,

        @JsonProperty("simpl:servicePolicy")
        ServicePolicyDto servicePolicy

) {}
