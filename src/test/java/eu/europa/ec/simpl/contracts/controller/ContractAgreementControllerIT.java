package eu.europa.ec.simpl.contracts.controller;

import eu.europa.ec.simpl.contracts.service.EDCConnector;
import eu.europa.ec.simpl.contracts.transfer.ContractResponseTO;
import eu.europa.ec.simpl.contracts.transfer.ContractSearchTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/database/ContractAgreementServiceConfirmationData.sql")
@EmbeddedKafka(partitions = 1, topics = {"search-contract"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource("/application.yaml")
@ExtendWith(OutputCaptureExtension.class)
class ContractAgreementControllerIT {

    private static final Long TIMEOUT = 10L;

    private static final String HOST = "http://localhost:%s/contract/v1/agreements/12345/search";

    @Value(value = "${local.server.port}")
    private int port;

    @MockitoBean
    private EDCConnector edcConnector;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setEdcConnector() {
        doNothing().when(edcConnector).notifyContractIsPresent(any(), anyBoolean(), any());
    }

    @ParameterizedTest
    @MethodSource("activeContractPresentByMultipleParameters")
    void testIsActiveContractPresentByMultipleParameters(ContractSearchTO contractSearchTO, CapturedOutput capturedOutput) {

        ResponseEntity<ContractResponseTO> response = restTemplate.exchange(
                String.format(HOST, port),
                HttpMethod.POST,
                prepareRequest(contractSearchTO),
                ContractResponseTO.class
        );

        assertNotNull(response, "Response is null");
        assertNotNull(response.getBody(), "Response body is null");
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Status code should be 2xx but is: " + response);
        await().atMost(TIMEOUT, TimeUnit.SECONDS)
                .until(() -> capturedOutput.getOut().contains("Contract search result (is present): true"));
    }

    static Stream<Arguments> activeContractPresentByMultipleParameters() {
        return Stream.of( ///
                arguments(new ContractSearchTO(UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2062"),
                        null, null, null, null)),
                arguments(new ContractSearchTO(UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2062"),
                        "807fdb6b-a9de-430d-9bb2-c5909b4b2064", null, null, null)),
                arguments(new ContractSearchTO(null, "807fdb6b-a9de-430d-9bb2-c5909b4b2064",
                        null, "X3", null)),
                arguments(new ContractSearchTO(null, null, "X2", "X3", "X4")));
    }

    @ParameterizedTest
    @MethodSource("activeContractNOTPresentByMultipleParameters")
    void testIsActiveContractNOTPresentByMultipleParameters(ContractSearchTO contractSearchTO, CapturedOutput capturedOutput) {

        ResponseEntity<ContractResponseTO> response = restTemplate.exchange(
                String.format(HOST, port),
                HttpMethod.POST,
                prepareRequest(contractSearchTO),
                ContractResponseTO.class
        );

        assertNotNull(response, "Response is null");
        assertNotNull(response.getBody(), "Response body is null");
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Status code should be 2xx but is: " + response);
        await().atMost(TIMEOUT, TimeUnit.SECONDS)
                .until(() -> capturedOutput.getOut().contains("Contract search result (is present): false"));
    }

    static Stream<Arguments> activeContractNOTPresentByMultipleParameters() {
        return Stream.of( ///
                arguments(new ContractSearchTO(UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2777"),
                        null, null, null, null)),
                arguments(new ContractSearchTO(UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2062"),
                        "807fdb6b-a9de-430d-9bb2-c5909b4b2777", null, null, null)),
                arguments(new ContractSearchTO(null, "807fdb6b-a9de-430d-9bb2-c5909b4b2064",
                        null, "X333", null)),
                arguments(new ContractSearchTO(null, null, "X222", "X3", "X4")));
    }

    @ParameterizedTest
    @MethodSource("activeContractPresentWithNotValidParameters")
    void testIsActiveContractPresentWithNotValidParameters(ContractSearchTO contractSearchTO) {

        ResponseEntity<Object> response = restTemplate.exchange(
                String.format(HOST, port),
                HttpMethod.POST,
                prepareRequest(contractSearchTO),
                Object.class
        );

        assertNotNull(response, "Response is null");
        assertNotNull(response.getBody(), "Response body is null");
        assertTrue(response.getStatusCode().is4xxClientError(), "Response code should be 4xx but is: " + response);
        assertTrue(response.getBody().toString().contains("Validation failure"),
                "Response should be of type: Validation failure");
    }

    static Stream<Arguments> activeContractPresentWithNotValidParameters() {
        return Stream.of( ///
                arguments(new ContractSearchTO(UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2062"),
                        "807fdb6b-a9de-430d-9bb2-c5909b%%$$064", null, null, null)),
                arguments(new ContractSearchTO(null, "807fdb6b-a9de-430d-9bb2-c5909b4b2064",
                        null, "X  3", null)),
                arguments(new ContractSearchTO(null, null, "%X2", "X3", "X4")),
                arguments(new ContractSearchTO(null, null, null, null, "X/4")));
    }

    @Test
    void testIsActiveContractPresentWithInvalidPathStructure() {

        ResponseEntity<Object> response = restTemplate.exchange(
                String.format(HOST, port),
                HttpMethod.POST,
                prepareRequest(new ContractSearchTO()),
                Object.class
        );

        assertNotNull(response, "Response is null");
        assertNotNull(response.getBody(), "Response body is null");
        assertTrue(response.getStatusCode().is4xxClientError(), "Response code should be 4xx but is: " + response);
        assertTrue(response.getBody().toString().contains("At least one search parameter must be provided correctly."),
                "Response should contain error: At least one search parameter must be provided correctly.");
    }

    private HttpEntity<ContractSearchTO> prepareRequest(ContractSearchTO contractSearchTO) {
        var headers = new HttpHeaders();
        headers.set("X-API-Key", "test");
        return new HttpEntity<>(contractSearchTO, headers);
    }

    @Test
    void testIsActiveContractPresentWithInvalidPathParameterNegotiationId() {

        var invalidContractNegotiationId = "test$$test";
        var path = "http://localhost:%s/contract/v1/agreements/" + invalidContractNegotiationId + "/search";

        ResponseEntity<Object> response = restTemplate.exchange(
                String.format(path, port),
                HttpMethod.POST,
                prepareRequest(new ContractSearchTO()),
                Object.class
        );

        assertNotNull(response, "Response is null");
        assertNotNull(response.getBody(), "Response body is null");
        assertTrue(response.getStatusCode().is4xxClientError(), "Response code should be 4xx but is: " + response);
        assertTrue(response.getBody().toString().contains("status=400"), "Response should contain error: status=400");
        assertTrue(response.getBody().toString().contains("detail=400 BAD_REQUEST"),
                "Response should contain detail=400 BAD_REQUEST");
        assertTrue(response.getBody().toString().contains("Validation failure"),
                "Response should contain error: Validation failure");
    }
}
