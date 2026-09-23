package eu.europa.ec.simpl.contracts.consumer;

import eu.europa.ec.simpl.contracts.events.StatusUpdateRequestEvent;
import eu.europa.ec.simpl.contracts.mapper.MessageMapper;
import eu.europa.ec.simpl.contracts.service.ContractAgreementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StatusUpdateConsumer {

    private final MessageMapper messageMapper;
    private final ContractAgreementService contractAgreementService;

    public StatusUpdateConsumer(MessageMapper messageMapper, ContractAgreementService contractAgreementService) {
        this.messageMapper = messageMapper;
        this.contractAgreementService = contractAgreementService;
    }

    @RetryableTopic(attempts = "10")
    @KafkaListener(topics = "${spring.kafka.topics.status-update}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        log.info("Received message");
        var event = messageMapper.createEvent(message, StatusUpdateRequestEvent.class);
        log.info(message);
        contractAgreementService.updateContractStatus(event.getContractAgreementId(),
                event.getContractDefinitionId(), event.getNewStatus());
        log.info("Contract confirmed for ID: {}", event.getContractAgreementId());
    }

    @KafkaListener(topics = "${spring.kafka.topics.status-update-dlt}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenDLT(String message) {

        log.info("Received malformed message on DLT");
        //add notification mechanism (e.g. with use of eu.europa.ec.simpl.notification_service)
    }
}
