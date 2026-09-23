package eu.europa.ec.simpl.contracts.transfer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogSearchItemDto(
        CatalogSearchNDto n
) {}
