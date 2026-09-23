package eu.europa.ec.simpl.contracts.service;

import eu.europa.ec.simpl.common.service.ExternalRequestService;
import eu.europa.ec.simpl.contracts.transfer.ContractSearchTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EDCConnector {

    @Value("${spring.external-services.urls.edc}")
    private String edcHost;

    private final ExternalRequestService externalRequestService;

    public void notifyContractSigned(String contractNegotiationId, boolean signed, UUID contractAgreementId) {

        final var path = "/signed/%s/%s".formatted(contractNegotiationId, signed);
        final var response = postRequest(path);
        log.info("Confirmation response for contractAgreementId {}: {}", contractAgreementId, response);
    }

    public void notifyContractIsPresent(String contractNegotiationId, boolean isPresent,
                                        ContractSearchTO contractSearchTO) {

        final var path = "/isPresent/%s/%s".formatted(contractNegotiationId, isPresent);
        final var response = postRequest(path);
        log.info("Confirmation response for contractNegotiationId {} and search parameters {}: {}",
                contractNegotiationId, contractSearchTO, response);
    }

    public void notifyCreateContractData(String contractNegotiationId, String contractData, UUID contractAgreementId) {

        final var path = "/contractData/%s".formatted(contractNegotiationId);
        final var response = postRequestWithBody(path, contractData);
        log.info("Confirmation response with contract data for contractAgreementId {} : {}",
                contractAgreementId, response);
    }

    private String postRequestWithBody(String path, String body) {

        final var bodySpec = externalRequestService.getBodySpecPost(edcHost, path, null);
        return bodySpec.bodyValue(body).retrieve().bodyToMono(String.class).block();
    }

    private String postRequest(String path) {

        final var bodySpec = externalRequestService.getBodySpecPost(edcHost, path, null);
        return bodySpec.retrieve().bodyToMono(String.class).block();
    }
}
