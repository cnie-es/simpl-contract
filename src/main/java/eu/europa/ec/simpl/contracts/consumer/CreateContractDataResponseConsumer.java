package eu.europa.ec.simpl.contracts.consumer;

import eu.europa.ec.simpl.contracts.events.CreateContractDataResponseEvent;
import eu.europa.ec.simpl.contracts.mapper.MessageMapper;
import eu.europa.ec.simpl.contracts.service.EDCConnector;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class CreateContractDataResponseConsumer {

    private final MessageMapper messageMapper;
    private final EDCConnector edcConnector;

    @RetryableTopic(attempts = "10")
    @KafkaListener(topics = "${spring.kafka.topics.create-contract-response}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        log.info("Received message");
        final var event = messageMapper.createEvent(message, CreateContractDataResponseEvent.class);
        log.info(message);
        log.info("Sending message about contract data for: {} for ID: {}",
                event.getContractNegotiationId(), event.getContractAgreementId());
        // call EDC Connector Control Plane endpoint
        edcConnector.notifyCreateContractData(event.getContractNegotiationId(),
                event.getResponseData(), event.getContractAgreementId());
    }

    @KafkaListener(topics = "${spring.kafka.topics.create-contract-response-dlt}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenDLT(String message) {

        log.info("Received malformed message on DLT");
    }
}
