package eu.europa.ec.simpl.contracts.service;

import eu.europa.ec.simpl.contracts.transfer.SignContractAgreementRequestTO;
import eu.europa.ec.simpl.contracts.utils.WebServerUtils;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
class SignContractServiceTest {

    @Autowired
    private SignContractService signContractService;

    private static MockWebServer mockSignerWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockSignerWebServer = WebServerUtils.createMockWebServer(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/v1/credential")) {
                    return new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .setBody(WebServerUtils.getFileContent("files/ca-response.json"))
                            .setResponseCode(200);
                }
                return new MockResponse()
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .setBody("NOT DEFINED")
                        .setResponseCode(500);
            }
        });
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockSignerWebServer.shutdown();
    }

    @Test
    void shouldSendAndReturnMockedResponse() {
        //given
        SignContractAgreementRequestTO requestTO = signContractService.generateRequest();

        //when
        String response = signContractService.sendToSigner(requestTO);

        //then
        assertTrue(response.contains("hello"));
        // todo: add some real tests for it
    }
}
