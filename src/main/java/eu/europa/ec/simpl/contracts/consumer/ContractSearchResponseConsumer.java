package eu.europa.ec.simpl.contracts.consumer;

import eu.europa.ec.simpl.contracts.events.ContractSearchResponseEvent;
import eu.europa.ec.simpl.contracts.mapper.MessageMapper;
import eu.europa.ec.simpl.contracts.service.ContractAgreementService;
import eu.europa.ec.simpl.contracts.service.EDCConnector;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class ContractSearchResponseConsumer {

    private final MessageMapper messageMapper;
    private final EDCConnector edcConnector;

    @RetryableTopic(attempts = "10")
    @KafkaListener(topics = "${spring.kafka.topics.search-contract-response}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {

        log.info("Received message");
        final var event = messageMapper.createEvent(message, ContractSearchResponseEvent.class);
        log.info(message);

        edcConnector.notifyContractIsPresent(event.getContractNegotiationId(), event.isPresent(),
                event.getContractSearchTO());
        log.info("Contract search result (is present): {}, for ContractNegotiationId: {} and parameters: {}",
                event.isPresent(), event.getContractNegotiationId(), event.getContractSearchTO());
    }

    @KafkaListener(topics = "${spring.kafka.topics.search-contract-response-dlt}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenDLT(String message) {

        log.info("Received malformed message on DLT");
        //add notification mechanism
        //(e.g. with use of eu.europa.ec.simpl.notification_service)
    }
}
