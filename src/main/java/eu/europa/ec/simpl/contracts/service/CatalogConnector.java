package eu.europa.ec.simpl.contracts.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.common.service.ExternalRequestService;
import eu.europa.ec.simpl.contracts.transfer.dto.CatalogSearchItemDto;
import eu.europa.ec.simpl.contracts.transfer.dto.CatalogSearchNDto;
import eu.europa.ec.simpl.contracts.transfer.dto.CatalogSearchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CatalogConnector {

    private static final String SEARCH_PATH = "v1/selfDescriptions/advancedSearch";
    @Value("${spring.external-services.urls.catalogSearch}")
    private String catalogSearchHost;

    @Value("${spring.external-services.urls.catalogGet}")
    private String catalogGetHost;

    private final ExternalRequestService externalRequestService;

    public String getContractDid(String assetId, String contractDefinitionId) {

        final String body = """
                {
                  "simpl:EdcRegistration": {
                    "assetId": "%s",
                    "contractDefinitionId": "%s"
                  }
                }""".formatted(assetId, contractDefinitionId);
        final var bodySpec = externalRequestService.getBodySpecPost(catalogSearchHost, SEARCH_PATH, null);
        final String result = bodySpec.bodyValue(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve().bodyToMono(String.class).block();
        // get data from result
        final String did = getDidFromJson(result);

        log.info("Catalog DID for assetId = {}, contractDefinitionId = {}: {}", assetId, contractDefinitionId, did);
        return did;
    }

    public String getContractData(String contractDid) {
        final var path = "self-descriptions/%s".formatted(contractDid);
        final var bodySpec = externalRequestService.getBodySpec(catalogGetHost, path, null);
        final String result = bodySpec.retrieve().bodyToMono(String.class).block();
        log.info("Catalog Contract Data get for DID {} - size: {}", contractDid,
                (result == null) ? "NULL" : result.length());
        return result;
    }

    private String getDidFromJson(String json) {
        final ObjectMapper mapper = new ObjectMapper();

        try {
            final CatalogSearchResponseDto response =
                    mapper.readValue(json, CatalogSearchResponseDto.class);

            return response.items().stream()
                    .findFirst()
                    .map(CatalogSearchItemDto::n)
                    .map(CatalogSearchNDto::claimsGraphUri)
                    .flatMap(list -> list.stream().findFirst())
                    .orElse(null);

        } catch (JsonProcessingException e) {
            log.error("Exception while processing response", e);
            return null;
        }
    }

}
