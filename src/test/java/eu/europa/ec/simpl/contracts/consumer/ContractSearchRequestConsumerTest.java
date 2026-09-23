package eu.europa.ec.simpl.contracts.consumer;

import eu.europa.ec.simpl.contracts.producer.MessageProducer;
import eu.europa.ec.simpl.contracts.service.ContractAgreementService;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1)
class ContractSearchRequestConsumerTest {

    private static final Long TIMEOUT = 5000L;

    @Value("${spring.kafka.topics.search-contract-request}")
    private String searchContractRequestTopic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private MessageProducer producer;

    @MockitoBean
    private ContractAgreementService contractAgreementService;

    @MockitoBean
    private EDCConnector edcConnector;

    @Test
    void testSearchContractRequest() {

        doNothing().when(edcConnector).notifyContractIsPresent(any(), anyBoolean(), any());

        var validMessage = """
                {
                  "contractNegotiationId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractSearchTO":
                    {
                      "contractDefinitionId": "123e4567-e89b-12d3-a456-426614174000",
                      "assetId": "X1",
                      "providerId": "X2",
                      "consumerId": "X3"
                    },
                  "mode": "PROVIDER"
                }
                """;

        kafkaTemplate.send(searchContractRequestTopic, validMessage);
        verify(contractAgreementService, timeout(TIMEOUT).times(1))
                .isActiveContractPresent(any());
    }

    @Test
    void testInvalidRequest(CapturedOutput capturedOutput) {

        var invalidMessage = """
                {
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractDefinitionId": "123e4567-e89b-12d3-a456-426614174000"
                }
                """;

        kafkaTemplate.send(searchContractRequestTopic, invalidMessage);
        await().atMost(TIMEOUT, TimeUnit.SECONDS)
                .until(() -> capturedOutput.getOut().contains("Received malformed message on DLT"));
    }


    @Test
    void testNotifyEDCError(CapturedOutput capturedOutput) {

        doThrow(new WebClientResponseException("Not Found", 404, "Test Exception", null, null, null))
                .when(edcConnector).notifyContractIsPresent(any(), anyBoolean(), any());

        var invalidMessage = """
                {
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractDefinitionId": "123e4567-e89b-12d3-a456-426614174000"
                }
                """;

        kafkaTemplate.send(searchContractRequestTopic, invalidMessage);
        await().atMost(TIMEOUT, TimeUnit.SECONDS)
                .until(() -> capturedOutput.getOut().contains("Received malformed message on DLT"));
    }
}
