package eu.europa.ec.simpl.contracts.consumer;

import eu.europa.ec.simpl.contracts.config.BusinessOperationsLogger;
import eu.europa.ec.simpl.contracts.events.ContractAgreementRequestEvent;
import eu.europa.ec.simpl.contracts.mapper.MessageMapper;
import eu.europa.ec.simpl.contracts.service.ContractAgreementService;
import eu.europa.ec.simpl.contracts.service.ContractSigningService;
import eu.europa.ec.simpl.contracts.service.PaymentApprovalService;
import eu.europa.ec.simpl.contracts.transfer.ContractAgreementCreateTO;
import eu.europa.ec.simpl.contracts.transfer.Mode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

import static eu.europa.ec.simpl.contracts.types.BusinessOperations.BP07_01;
import static eu.europa.ec.simpl.common_logging.types.MessageType.REQUEST;

@Component
@Slf4j
public class SignContractRequestConsumer {

    private final MessageMapper messageMapper;
    private final ContractAgreementService contractAgreementService;
    private final ContractSigningService contractSigningService;
    private final PaymentApprovalService paymentApprovalService;

    @Value("${spring.mode}")
    private String mode;

    public SignContractRequestConsumer(MessageMapper messageMapper,
                                       ContractAgreementService contractAgreementService,
                                       ContractSigningService contractSigningService,
                                       PaymentApprovalService paymentApprovalService) {
        this.messageMapper = messageMapper;
        this.contractAgreementService = contractAgreementService;
        this.contractSigningService = contractSigningService;
        this.paymentApprovalService = paymentApprovalService;
    }

    @RetryableTopic(attempts = "10")
    @KafkaListener(topics = "${spring.kafka.topics.sign-contract-request}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        log.info("Received message");
        var event = messageMapper.createEvent(message, ContractAgreementRequestEvent.class);
        log.info(message);
        BusinessOperationsLogger.log(BP07_01, REQUEST, event.getContractAgreementId(),
                "Sign contract request for ID: ");

        final ContractAgreementCreateTO createTO = event.getContractAgreementCreateTO();

        // store data record before signature
        if (Mode.PROVIDER.name().equals(mode)) {
            contractAgreementService.createAndSaveContractAgreement(event.getContractAgreementId(),
                    event.getContractDefinitionId(), createTO);

            if (paymentApprovalService.shouldHoldForPayment(createTO.getPriceType())) {
                paymentApprovalService.markPendingPayment(event.getContractAgreementId());
                log.info("Contract agreement {} is a paid offer; holding negotiation {} until payment "
                                + "is confirmed by the provider.",
                        event.getContractAgreementId(), createTO.getContractNegotiationId());
                return;
            }
        }

        contractSigningService.signAndPublishResponse(event.getContractAgreementId(),
                createTO.getContractNegotiationId());
    }

    @KafkaListener(topics = "${spring.kafka.topics.sign-contract-request-dlt}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenDLT(String message) {

        log.info("Received malformed message on DLT");
        //add notification mechanism (e.g. with use of eu.europa.ec.simpl.notification_service)
    }
}
