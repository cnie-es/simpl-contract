package eu.europa.ec.simpl.contracts.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.contracts.events.CreateContractDataRequestEvent;
import eu.europa.ec.simpl.contracts.events.CreateContractDataResponseEvent;
import eu.europa.ec.simpl.contracts.mapper.MessageMapper;
import eu.europa.ec.simpl.contracts.producer.MessageProducer;
import eu.europa.ec.simpl.contracts.service.ContractDataService;
import eu.europa.ec.simpl.contracts.transfer.ContractDataResponseTO;
import eu.europa.ec.simpl.contracts.transfer.Mode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

import static eu.europa.ec.simpl.contracts.kafka.KafkaTopic.CREATE_CONTRACT_RESPONSE;

@Component
@Slf4j
public class CreateContractDataRequestConsumer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final MessageProducer messageProducer;
    private final MessageMapper messageMapper;
    private final ContractDataService contractDataService;

    @Value("${spring.mode}")
    private String mode;

    public CreateContractDataRequestConsumer(MessageProducer messageProducer, MessageMapper messageMapper,
                                             ContractDataService contractDataService) {
        this.messageProducer = messageProducer;
        this.messageMapper = messageMapper;
        this.contractDataService = contractDataService;
    }

    @RetryableTopic(attempts = "10")
    @KafkaListener(topics = "${spring.kafka.topics.create-contract-request}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        log.info("Received message");
        final var event = messageMapper.createEvent(message, CreateContractDataRequestEvent.class);
        log.info(message);
        // prepare data
        final ContractDataResponseTO responseData = contractDataService.createContractDataResponse(
                event.getContractAgreementCreateTO().getContractNegotiationId(),
                event.getContractDefinitionId(),
                event.getContractAgreementCreateTO().getAssetId()
                );
        // parse message into string
        final String responseDataString;
        try {
            responseDataString = OBJECT_MAPPER.writeValueAsString(responseData);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            return;
        }
        // send response to kafka
        final var responseEvent = new CreateContractDataResponseEvent(event.getContractAgreementId(),
                event.getContractAgreementCreateTO().getContractNegotiationId(), Mode.valueOf(mode),
                responseDataString);
        messageProducer.sendMessage(CREATE_CONTRACT_RESPONSE, responseEvent);
    }

    @KafkaListener(topics = "${spring.kafka.topics.create-contract-request-dlt}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenDLT(String message) {

        log.info("Received malformed message on DLT");
    }
}
