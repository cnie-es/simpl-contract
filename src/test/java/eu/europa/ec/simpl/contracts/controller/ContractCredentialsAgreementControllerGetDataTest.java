package eu.europa.ec.simpl.contracts.controller;

import eu.europa.ec.simpl.contracts.AbstractUnitTest;
import eu.europa.ec.simpl.contracts.transfer.ContractAgreementCreateTO;
import eu.europa.ec.simpl.contracts.transfer.ContractResponseTO;
import eu.europa.ec.simpl.contracts.types.ContractAgreementStatusType;
import eu.europa.ec.simpl.contracts.utils.WebServerUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DirtiesContext
@ExtendWith(OutputCaptureExtension.class)
@EmbeddedKafka(partitions = 1, topics = {"create-contract-req", "create-contract-resp"})
class ContractCredentialsAgreementControllerGetDataTest extends AbstractUnitTest {

    private static MockWebServer mockSignerWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockSignerWebServer = WebServerUtils.createMockWebServer(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/v1/selfDescriptions/advancedSearch")) {
                    String requestBody = request.getBody().readString(Charset.defaultCharset());
                    String responseBody = WebServerUtils.getFileContent("files/catalog-search-response.json");
                    if (requestBody.contains("badAsset")) {
                        responseBody = "{xxx";
                    } else if (requestBody.contains("assetParsing")) {
                        responseBody = WebServerUtils.getFileContent("files/catalog-search-response-wrong.json");
                    }
                    return new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .setBody(responseBody)
                            .setResponseCode(200);
                } else if (request.getPath().contains("/self-descriptions/")) {
                    String responseBody = WebServerUtils.getFileContent("files/catalog-get-response.json");
                    if (request.getPath().contains("but-with-error")) {
                        responseBody = "xxxx";
                    }
                    return new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .setBody(responseBody)
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

    @Autowired
    private ContractCredentialsAgreementController contractCredentialsAgreementController;

    @Test
    void shouldProcessFullFlow(CapturedOutput output) {
        //given
        UUID contractId = UUID.fromString("d86f352e-2154-4133-a24a-a9a847cbaced");
        String definitionId = "d86f352e-2154-4133-a24a-a9a847cbac12";
        ContractAgreementCreateTO contractAgreementCreateTO = new ContractAgreementCreateTO();
        contractAgreementCreateTO.setAssetId("someAsset");
        contractAgreementCreateTO.setContractNegotiationId("strongNegotiations");
        contractAgreementCreateTO.setContractOfferId("bestSeller");
        contractAgreementCreateTO.setConsumerId("me");
        contractAgreementCreateTO.setProviderId("myMom");

        //when
        ResponseEntity<ContractResponseTO> responseEntity = contractCredentialsAgreementController.createVerifiableCredential(contractId, definitionId, contractAgreementCreateTO);

        //then
        assertEquals(contractId, responseEntity.getBody().getContractAgreementId());
        assertEquals(definitionId, responseEntity.getBody().getContractDefinitionId());
        assertEquals(ContractAgreementStatusType.INITIATED, responseEntity.getBody().getStatus());
        await().until(() -> output.getOut().contains("CreateContractDataRequestConsumer - Received message"));
        await().until(() -> output.getOut().contains("CreateContractDataResponseConsumer - Sending message about contract data for: %s for ID: %s"
                .formatted(contractAgreementCreateTO.getContractNegotiationId(), contractId)));
    }


    @Test
    void shouldFailOnWrongResponse(CapturedOutput output) {
        //given
        UUID contractId = UUID.fromString("d86f352e-2154-4133-a24a-a9a847cbaced");
        String definitionId = "d86f352e-2154-4133-a24a-a9a847cbac12";
        ContractAgreementCreateTO contractAgreementCreateTO = new ContractAgreementCreateTO();
        contractAgreementCreateTO.setAssetId("badAsset");
        contractAgreementCreateTO.setContractNegotiationId("strongNegotiations");
        contractAgreementCreateTO.setContractOfferId("bestSeller");
        contractAgreementCreateTO.setConsumerId("me");
        contractAgreementCreateTO.setProviderId("myMom");

        //when
        ResponseEntity<ContractResponseTO> responseEntity = contractCredentialsAgreementController.createVerifiableCredential(contractId, definitionId, contractAgreementCreateTO);

        //then
        assertEquals(contractId, responseEntity.getBody().getContractAgreementId());
        assertEquals(definitionId, responseEntity.getBody().getContractDefinitionId());
        assertEquals(ContractAgreementStatusType.INITIATED, responseEntity.getBody().getStatus());
        await().until(() -> output.getOut().contains("CreateContractDataRequestConsumer - Received message"));
        await().until(() -> output.getOut().contains("CatalogConnector - Exception while processing response"));
    }

    @Test
    void shouldFailOnParsingWrongJson(CapturedOutput output) {
        //given
        UUID contractId = UUID.fromString("d86f352e-2154-4133-a24a-a9a847cbaced");
        String definitionId = "d86f352e-2154-4133-a24a-a9a847cbac12";
        ContractAgreementCreateTO contractAgreementCreateTO = new ContractAgreementCreateTO();
        contractAgreementCreateTO.setAssetId("assetParsing");
        contractAgreementCreateTO.setContractNegotiationId("strongNegotiations");
        contractAgreementCreateTO.setContractOfferId("bestSeller");
        contractAgreementCreateTO.setConsumerId("me");
        contractAgreementCreateTO.setProviderId("myMom");

        //when
        ResponseEntity<ContractResponseTO> responseEntity = contractCredentialsAgreementController.createVerifiableCredential(contractId, definitionId, contractAgreementCreateTO);

        //then
        assertEquals(contractId, responseEntity.getBody().getContractAgreementId());
        assertEquals(definitionId, responseEntity.getBody().getContractDefinitionId());
        assertEquals(ContractAgreementStatusType.INITIATED, responseEntity.getBody().getStatus());
        await().until(() -> output.getOut().contains("CreateContractDataRequestConsumer - Received message"));
        await().until(() -> output.getOut().contains("Exception in human readable format generation"));
    }

}
