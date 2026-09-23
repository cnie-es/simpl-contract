package eu.europa.ec.simpl.contracts.producer;

import eu.europa.ec.simpl.contracts.events.ContractEvent;
import eu.europa.ec.simpl.contracts.kafka.KafkaTopic;
import eu.europa.ec.simpl.contracts.mapper.MessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MessageProducer {

    private final MessageMapper messageMapper;

    @Value("${spring.kafka.topics.sign-contract-response}")
    private String signContractResponseTopic;

    @Value("${spring.kafka.topics.sign-contract-request}")
    private String signContractRequestTopic;

    @Value("${spring.kafka.topics.status-update}")
    private String statusUpdateTopic;

    @Value("${spring.kafka.topics.search-contract-request}")
    private String searchActiveContractRequestTopic;

    @Value("${spring.kafka.topics.search-contract-response}")
    private String searchActiveContractResponseTopic;

    @Value("${spring.kafka.topics.create-contract-request}")
    private String createContractRequestTopic;

    @Value("${spring.kafka.topics.create-contract-response}")
    private String createContractResponseTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public MessageProducer(MessageMapper messageMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.messageMapper = messageMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public boolean sendMessage(KafkaTopic topic, ContractEvent event) {

        log.info("Sending message to topic: {}", topic);
        final var result = kafkaTemplate.send(resolve(topic), messageMapper.convertEventIntoStringMessage(event));
        return handleResult(result);
    }

    private static Boolean handleResult(CompletableFuture<SendResult<String, String>> result) {

        result.handle((success, exception) -> {
            if (Objects.nonNull(exception)) {
                log.error("Error when sending message: {}", exception.getMessage());
            } else {
                log.info("Message successfully sent");
            }
            return null;
        });
        return !result.isCompletedExceptionally();
    }

    private String resolve(KafkaTopic topic) {
        return switch (topic) {
            case STATUS_UPDATE -> statusUpdateTopic;
            case SIGN_CONTRACT_REQUEST -> signContractRequestTopic;
            case SIGN_CONTRACT_RESPONSE -> signContractResponseTopic;
            case SEARCH_ACTIVE_CONTRACT_REQUEST -> searchActiveContractRequestTopic;
            case SEARCH_ACTIVE_CONTRACT_RESPONSE -> searchActiveContractResponseTopic;
            case CREATE_CONTRACT_REQUEST -> createContractRequestTopic;
            case CREATE_CONTRACT_RESPONSE -> createContractResponseTopic;
        };
    }
}
