package eu.europa.ec.simpl.contracts.consumer;

import eu.europa.ec.simpl.contracts.events.ContractSearchRequestEvent;
import eu.europa.ec.simpl.contracts.events.ContractSearchResponseEvent;
import eu.europa.ec.simpl.contracts.kafka.KafkaTopic;
import eu.europa.ec.simpl.contracts.mapper.MessageMapper;
import eu.europa.ec.simpl.contracts.producer.MessageProducer;
import eu.europa.ec.simpl.contracts.service.ContractAgreementService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class ContractSearchRequestConsumer {

    private final MessageProducer messageProducer;
    private final MessageMapper messageMapper;
    private final ContractAgreementService contractAgreementService;

    @RetryableTopic(attempts = "10")
    @KafkaListener(topics = "${spring.kafka.topics.search-contract-request}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {

        log.info("Received message");
        final var event = messageMapper.createEvent(message, ContractSearchRequestEvent.class);
        log.info(message);

        final var isPresent = contractAgreementService.isActiveContractPresent(event.getContractSearchTO());

        log.info("Contract search result (is present): {}, for ContractNegotiationID: {}, and search params: {}",
                isPresent, event.getContractNegotiationId(), event.getContractSearchTO());

        final var responseEvent = new ContractSearchResponseEvent(event.getContractNegotiationId(),
                event.getContractSearchTO(), isPresent);

        messageProducer.sendMessage(KafkaTopic.SEARCH_ACTIVE_CONTRACT_RESPONSE, responseEvent);
    }

    @KafkaListener(topics = "${spring.kafka.topics.search-contract-request-dlt}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenDLT(String message) {

        log.info("Received malformed message on DLT");
        //add notification mechanism
        //(e.g. with use of eu.europa.ec.simpl.notification_service)
    }
}
