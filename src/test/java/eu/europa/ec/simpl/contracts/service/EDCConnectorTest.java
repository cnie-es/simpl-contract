package eu.europa.ec.simpl.contracts.service;

import eu.europa.ec.simpl.common.security.secret.SecretClientRetriever;
import eu.europa.ec.simpl.common.service.ExternalRequestService;
import eu.europa.ec.simpl.contracts.transfer.ContractSearchTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class EDCConnectorTest {

    private static final Long TIMEOUT = 10L;

    private static final String URL = "/test.url.for.EDCConnector/";

    private static final EDCConnector edcConnector = new EDCConnector(new ExternalRequestService
            (new SecretClientRetriever("test")));

    @Test
    void testNotifyContractSigned(CapturedOutput capturedOutput) throws IOException {

        var contractAgreementId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        try (var server = new MockWebServer()) {
            var response = new MockResponse().addHeader("Content-Type:application/json").setBody("test");
            server.enqueue(response);
            server.start();
            ReflectionTestUtils.setField(edcConnector, "edcHost", server.url(URL).toString());

            edcConnector.notifyContractSigned("testID", true, contractAgreementId);
            await().atMost(TIMEOUT, TimeUnit.SECONDS)
                    .until(() -> capturedOutput.getOut().contains("Confirmation response for contractAgreementId"));
        }
    }

    @Test
    void testNotifyContractIsPresent(CapturedOutput capturedOutput) throws IOException {

        var contractSearchTO = new ContractSearchTO();

        try (var server = new MockWebServer()) {
            var response = new MockResponse().addHeader("Content-Type:application/json").setBody("test");
            server.enqueue(response);
            server.start();
            ReflectionTestUtils.setField(edcConnector, "edcHost", server.url(URL).toString());

            edcConnector.notifyContractIsPresent("testID", true, contractSearchTO);
            await().atMost(TIMEOUT, TimeUnit.SECONDS)
                    .until(() -> capturedOutput.getOut().contains("Confirmation response for contractNegotiationId"));
        }
    }

    @Test
    void testNotifyEDCError() throws IOException {

        var contractAgreementId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        try (var server = new MockWebServer()) {
            var response = new MockResponse().addHeader("Content-Type:application/json").setResponseCode(404);
            server.enqueue(response);
            server.start();
            ReflectionTestUtils.setField(edcConnector, "edcHost", server.url(URL).toString());

            assertThrows(RuntimeException.class, () -> edcConnector.notifyContractSigned("testID", true,
                    contractAgreementId), "Should have failed");
        }
    }
}
