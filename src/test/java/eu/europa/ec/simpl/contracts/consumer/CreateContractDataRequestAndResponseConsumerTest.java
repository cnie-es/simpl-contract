package eu.europa.ec.simpl.contracts.consumer;

import eu.europa.ec.simpl.contracts.producer.MessageProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1)
class CreateContractDataRequestAndResponseConsumerTest {

    private static final Long TIMEOUT = 5000L;

    @Value("${spring.kafka.topics.create-contract-request}")
    private String createContractRequestTopic;

    @Value("${spring.kafka.topics.create-contract-response}")
    private String createContractResponseTopic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private MessageProducer producer;

    @Test
    void testInvalidRequest(CapturedOutput capturedOutput) {
        var invalidMessage = """
                {
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractDefinitionId": "123e4567-e89b-12d3-a456-426614174000",
                  "mode": "CONSUMER",
                  "contractAgreementCreateTO": {}
                }
                """;

        kafkaTemplate.send(createContractRequestTopic, invalidMessage);
        await().atMost(TIMEOUT, TimeUnit.SECONDS)
                .until(() -> capturedOutput.getOut().contains("Received malformed message on DLT"));
    }


    @Test
    void testInvalidResponse(CapturedOutput capturedOutput) {
        var invalidMessage = """
                {
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractDefinitionId": "123e4567-e89b-12d3-a456-426614174000",
                  "mode": "CONSUMER",
                  "contractAgreementCreateTO": {}
                }
                """;

        kafkaTemplate.send(createContractResponseTopic, invalidMessage);
        await().atMost(TIMEOUT, TimeUnit.SECONDS)
                .until(() -> capturedOutput.getOut().contains("Received malformed message on DLT"));
    }
}
