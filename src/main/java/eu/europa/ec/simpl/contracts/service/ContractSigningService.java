package eu.europa.ec.simpl.contracts.service;

import eu.europa.ec.simpl.common.exceptions.ResponseStatusSingleException;
import eu.europa.ec.simpl.contracts.config.BusinessOperationsLogger;
import eu.europa.ec.simpl.contracts.events.ContractAgreementResponseEvent;
import eu.europa.ec.simpl.contracts.kafka.KafkaTopic;
import eu.europa.ec.simpl.contracts.producer.MessageProducer;
import eu.europa.ec.simpl.contracts.transfer.Mode;
import eu.europa.ec.simpl.contracts.transfer.SignContractAgreementRequestTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static eu.europa.ec.simpl.common_logging.types.MessageType.RESPONSE;
import static eu.europa.ec.simpl.contracts.types.BusinessOperations.BP07_01;

/**
 * Encapsulates the signing of a contract agreement and the emission of the
 * {@code SIGN_CONTRACT_RESPONSE} event back to the EDC connector.
 *
 * <p>This logic is shared between the automatic path ({@code SignContractRequestConsumer} for free
 * offerings) and the manual path ({@code PaymentApprovalService} once a payment has been confirmed),
 * so that both produce exactly the same callback to the connector.</p>
 */
@Service
@Slf4j
public class ContractSigningService {

    private final SignContractService signContractService;
    private final ContractAgreementService contractAgreementService;
    private final MessageProducer messageProducer;

    @Value("${spring.mode}")
    private String mode;

    public ContractSigningService(SignContractService signContractService,
                                  ContractAgreementService contractAgreementService,
                                  MessageProducer messageProducer) {
        this.signContractService = signContractService;
        this.contractAgreementService = contractAgreementService;
        this.messageProducer = messageProducer;
    }

    /**
     * Signs the contract and publishes the response that releases the negotiation on the connector.
     * On a signing failure a {@code signed=false} response is published so the negotiation is
     * terminated instead of left hanging.
     *
     * @param contractAgreementId   the agreement to sign
     * @param contractNegotiationId the related negotiation id (for the response event)
     */
    public void signAndPublishResponse(UUID contractAgreementId, String contractNegotiationId) {
        boolean signed = false;
        try {
            final SignContractAgreementRequestTO request = signContractService.generateRequest();
            final String response = signContractService.sendToSigner(request);
            log.debug("Signer response: {}", response);

            if (Mode.PROVIDER.name().equals(mode)) {
                contractAgreementService.updateContractAgreementProviderDateAndStatus(contractAgreementId);
            }
            signed = true;
            BusinessOperationsLogger.log(BP07_01, RESPONSE, contractAgreementId, "Contract signed for ID: ");
        } catch (ResponseStatusSingleException e) {
            log.error("Failed to sign contract", e);
        }

        publish(contractAgreementId, contractNegotiationId, signed);
    }

    /**
     * Publishes a {@code signed=false} response so the connector terminates the negotiation, without
     * attempting to sign. Used when a payment is rejected or its confirmation window expires.
     *
     * @param contractAgreementId   the agreement to reject
     * @param contractNegotiationId the related negotiation id (for the response event)
     */
    public void publishRejection(UUID contractAgreementId, String contractNegotiationId) {
        publish(contractAgreementId, contractNegotiationId, false);
    }

    private void publish(UUID contractAgreementId, String contractNegotiationId, boolean signed) {
        final var responseEvent = new ContractAgreementResponseEvent(contractAgreementId,
                contractNegotiationId, Mode.valueOf(mode), signed);
        messageProducer.sendMessage(KafkaTopic.SIGN_CONTRACT_RESPONSE, responseEvent);
    }
}
