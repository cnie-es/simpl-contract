package eu.europa.ec.simpl.contracts.consumer;

import eu.europa.ec.simpl.contracts.events.ContractAgreementResponseEvent;
import eu.europa.ec.simpl.contracts.service.EDCConnector;
import eu.europa.ec.simpl.contracts.mapper.MessageMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class SignContractResponseConsumer {

    private final MessageMapper messageMapper;
    private final EDCConnector edcConnector;

    @RetryableTopic(attempts = "10")
    @KafkaListener(topics = "${spring.kafka.topics.sign-contract-response}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        log.info("Received message");
        var event = messageMapper.createEvent(message, ContractAgreementResponseEvent.class);
        log.info(message);
        log.info("Sending message about contract signature for: {} for ID: {}",
                event.getContractNegotiationId(), event.getContractAgreementId());
        // call EDC Connector Control Plane endpoint
        edcConnector.notifyContractSigned(event.getContractNegotiationId(),
                event.isSigned(), event.getContractAgreementId());
    }

    @KafkaListener(topics = "${spring.kafka.topics.sign-contract-response-dlt}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenDLT(String message) {

        log.info("Received malformed message on DLT");
        //add notification mechanism (e.g. with use of eu.europa.ec.simpl.notification_service)
    }
}
