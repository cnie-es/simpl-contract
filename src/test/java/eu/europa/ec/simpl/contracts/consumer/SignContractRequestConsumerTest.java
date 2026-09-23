package eu.europa.ec.simpl.contracts.consumer;

import eu.europa.ec.simpl.contracts.producer.MessageProducer;
import eu.europa.ec.simpl.contracts.service.ContractAgreementService;
import eu.europa.ec.simpl.contracts.service.SignContractService;
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
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1)
class SignContractRequestConsumerTest {

    private static final Long TIMEOUT = 5000L;

    @Value("${spring.kafka.topics.sign-contract-request}")
    private String signContractRequestTopic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private MessageProducer producer;

    @MockitoBean
    private ContractAgreementService contractAgreementService;

    @MockitoBean
    private SignContractService signContractService;

    @Test
    void testSignContractRequest() {
        var validMessage = """
                {
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractDefinitionId": "123e4567-e89b-12d3-a456-426614174000",
                  "mode": "CONSUMER",
                  "contractAgreementCreateTO": {
                    "contractNegotiationId": "1234"
                  }
                }
                """;

        kafkaTemplate.send(signContractRequestTopic, validMessage);
        verify(signContractService, timeout(TIMEOUT).times(1)).generateRequest();
    }

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

        kafkaTemplate.send(signContractRequestTopic, invalidMessage);
        await().atMost(TIMEOUT, TimeUnit.SECONDS)
                .until(() -> capturedOutput.getOut().contains("Received malformed message on DLT"));
    }
}
