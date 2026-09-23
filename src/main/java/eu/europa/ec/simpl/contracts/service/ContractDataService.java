package eu.europa.ec.simpl.contracts.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europa.ec.simpl.contracts.mapper.ContractHumanReadableRenderer;
import eu.europa.ec.simpl.contracts.transfer.ContractDataResponseTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ContractDataService {

    private static final int ERROR_CODE_NOT_FOUND = 404;
    private static final int ERROR_CODE_FAILURE = 500;
    private final CatalogConnector catalogConnector;
    private final DocumentHashService documentHashService;
    private final ContractHumanReadableRenderer contractHumanReadableRenderer;

    public ContractDataService(CatalogConnector catalogConnector,
                               DocumentHashService documentHashService,
                               ContractHumanReadableRenderer contractHumanReadableRenderer) {
        this.catalogConnector = catalogConnector;
        this.documentHashService = documentHashService;
        this.contractHumanReadableRenderer = contractHumanReadableRenderer;
    }

    /**
     * Create contract data
     * @param contractNegotiationId - negotiation id - only as reference
     * @param contractDefinitionId - definition id used to search in catalog
     * @param assetId - asset id used to search in catalog
     * @return - errorCode = 0 for success
     */
    public ContractDataResponseTO createContractDataResponse(String contractNegotiationId,
                                                             String contractDefinitionId,
                                                             String assetId) {
        // find contract
        log.info("Creating contract data for definition: {}, asset: {}, negotiationId: {}",
                contractDefinitionId, assetId, contractNegotiationId);
        final String contractDid = catalogConnector.getContractDid(assetId, contractDefinitionId);
        log.debug("ContractDid: {}", contractDid);

        final ContractDataResponseTO response = new ContractDataResponseTO();
        final String contractData;
        if (contractDid != null) {
            contractData = catalogConnector.getContractData(contractDid);
            log.debug("Contract data size: {}", contractData.length());
        } else {
            response.setErrorMessage("Contract data not found for definitionId: %s, assetId: %s"
                    .formatted(contractDefinitionId, assetId));
            response.setErrorCode(ERROR_CODE_NOT_FOUND);
            return response;
        }
        response.setResponse(contractData);
        response.setContractNegotiationId(contractNegotiationId);
        log.debug("Prepare human readable format");
        final String humanReadable;
        try {
            humanReadable = contractHumanReadableRenderer.render(contractData, contractNegotiationId);
        } catch (JsonProcessingException e) {
            response.setErrorCode(ERROR_CODE_FAILURE);
            response.setErrorMessage("Exception in human readable format generation: %s"
                    .formatted(e.getMessage()));
            return response;
        }
        log.debug("Human readable format size: {}", humanReadable.length());
        response.setHumanReadable(humanReadable.replace("\n", ""));
        log.debug("Prepare hash");
        response.setHash(documentHashService.calculateHash(humanReadable));
        response.setErrorCode(0);
        log.info("Contract data prepared");
        return response;
    }
}
