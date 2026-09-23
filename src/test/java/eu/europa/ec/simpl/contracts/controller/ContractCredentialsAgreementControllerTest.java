package eu.europa.ec.simpl.contracts.controller;

import eu.europa.ec.simpl.contracts.AbstractUnitTest;
import eu.europa.ec.simpl.contracts.entity.ContractAgreement;
import eu.europa.ec.simpl.contracts.repository.ContractAgreementRepository;
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
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DirtiesContext
@ExtendWith(OutputCaptureExtension.class)
@EmbeddedKafka(partitions = 1, topics = {"sign-contract-req", "sign-contract-resp", "status-update"})
@Sql("/database/ContractAgreementServiceConfirmationData.sql")
class ContractCredentialsAgreementControllerTest extends AbstractUnitTest {

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
                } else if (request.getPath().contains("/")) {
                    return new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .setBody("OK")
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

    @Autowired
    private ContractAgreementRepository contractAgreementRepository;

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
        ResponseEntity<ContractResponseTO> responseEntity = contractCredentialsAgreementController.issueVerifiableCredential(contractId, definitionId, contractAgreementCreateTO);

        //then
        assertEquals(contractId, responseEntity.getBody().getContractAgreementId());
        assertEquals(definitionId, responseEntity.getBody().getContractDefinitionId());
        assertEquals(ContractAgreementStatusType.INITIATED, responseEntity.getBody().getStatus());
        await().until(() -> output.getOut().contains("Sending message about contract signature for: strongNegotiations"));
        await().until(() -> output.getOut().contains("Confirmation response for contractAgreementId " + contractId + ":"));

        List<ContractAgreement> dbData = contractAgreementRepository.findAll();
        assertThat(dbData)
                .hasSize(6)
                .extracting(ContractAgreement::getContractAgreementId,
                        ContractAgreement::getContractDefinitionId,
                        ContractAgreement::getStatus,
                        ContractAgreement::getAssetId,
                        ContractAgreement::getContractNegotiationId,
                        ContractAgreement::getContractOfferId,
                        ContractAgreement::getConsumerId,
                        ContractAgreement::getProviderId)
                .contains(
                        tuple(contractId, definitionId, ContractAgreementStatusType.CREATED,
                                contractAgreementCreateTO.getAssetId(),
                                contractAgreementCreateTO.getContractNegotiationId(),
                                contractAgreementCreateTO.getContractOfferId(),
                                contractAgreementCreateTO.getConsumerId(),
                                contractAgreementCreateTO.getProviderId()
                        )
                );

    }

}
