package eu.europa.ec.simpl.contracts.consumer;

import eu.europa.ec.simpl.contracts.service.EDCConnector;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1)
class SignContractResponseConsumerTest {

    private static final Long TIMEOUT = 5000L;

    @Value("${spring.kafka.topics.sign-contract-response}")
    private String signContractResponseTopic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private EDCConnector edcConnector;

    @Test
    void testSignContractResponse() {

        doNothing().when(edcConnector).notifyContractSigned(any(), anyBoolean(), any());

        var validMessage = """
                {
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractNegotiationId": "001",
                  "mode": "CONSUMER",
                  "signed": true
                }
                """;

        kafkaTemplate.send(signContractResponseTopic, validMessage);
        verify(edcConnector, timeout(TIMEOUT).times(1)).notifyContractSigned(any(), anyBoolean(), any());
    }

    @Test
    void testInvalidContractResponse(CapturedOutput capturedOutput) {
        var invalidMessage = """
                {
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractDefinitionId": "123e4567-e89b-12d3-a456-426614174000",
                }
                """;

        kafkaTemplate.send(signContractResponseTopic, invalidMessage);
        await().atMost(TIMEOUT, TimeUnit.SECONDS)
                .until(() -> capturedOutput.getOut().contains("Received malformed message on DLT"));
    }

    @Test
    void testNotifyEDCError(CapturedOutput capturedOutput) {

        doThrow(new WebClientResponseException("Not Found", 404, "Test Exception", null, null, null))
                .when(edcConnector).notifyContractSigned(any(), anyBoolean(), any());

        var validMessage = """
                {
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractNegotiationId": "001",
                  "mode": "CONSUMER",
                  "signed": true
                }
                """;

        kafkaTemplate.send(signContractResponseTopic, validMessage);
        await().atMost(TIMEOUT, TimeUnit.SECONDS)
                .until(() -> capturedOutput.getOut().contains("Received malformed message on DLT"));
    }
}
