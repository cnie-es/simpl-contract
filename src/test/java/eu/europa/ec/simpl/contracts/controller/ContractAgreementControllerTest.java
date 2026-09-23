package eu.europa.ec.simpl.contracts.controller;

import eu.europa.ec.simpl.common.exceptions.BadArgumentException;
import eu.europa.ec.simpl.common.exceptions.RecordNotFoundException;
import eu.europa.ec.simpl.contracts.AbstractUnitTest;
import eu.europa.ec.simpl.contracts.entity.ContractAgreement;
import eu.europa.ec.simpl.contracts.repository.ContractAgreementRepository;
import eu.europa.ec.simpl.contracts.service.EDCConnector;
import eu.europa.ec.simpl.contracts.transfer.ContractAgreementTO;
import eu.europa.ec.simpl.contracts.transfer.ContractResponseTO;
import eu.europa.ec.simpl.contracts.transfer.ContractSearchConfirmationTO;
import eu.europa.ec.simpl.contracts.transfer.ContractSearchTO;
import eu.europa.ec.simpl.contracts.transfer.StatusUpdateRequestTO;
import eu.europa.ec.simpl.contracts.types.ContractAgreementStatusType;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;

@Slf4j
@DirtiesContext
@ExtendWith(OutputCaptureExtension.class)
@EmbeddedKafka(partitions = 1, topics = {"status-update"})
@Sql("/database/ContractAgreementServiceConfirmationData.sql")
class ContractAgreementControllerTest extends AbstractUnitTest {

    private static final Long TIMEOUT = 10L;

    private static final String CONTRACT_NEGOTIATION_ID = "807fdb6b-a9de-430d-9bb2-c5909b4b2064";

    @MockitoBean
    private EDCConnector edcConnector;

    @Autowired
    private ContractAgreementController contractAgreementController;

    @Autowired
    private ContractAgreementRepository contractAgreementRepository;

    @Test
    void shouldProcessFullFlow(CapturedOutput output) {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2062");
        String definitionId = "807fdb6b-a9de-430d-9bb2-c5909b4b2064";
        StatusUpdateRequestTO status = new StatusUpdateRequestTO("TERMINATED");

        //when
        ResponseEntity<ContractResponseTO> responseEntity = contractAgreementController.updateContractAgreementStatus(contractId, definitionId, status);

        //then
        assertEquals(contractId, responseEntity.getBody().getContractAgreementId());
        assertEquals(definitionId, responseEntity.getBody().getContractDefinitionId());
        assertEquals(ContractAgreementStatusType.FINALIZING, responseEntity.getBody().getStatus());
        await().until(() -> output.getOut().contains("Contract confirmed"));
        List<ContractAgreement> dbData = contractAgreementRepository.findAll();
        assertThat(dbData)
                .hasSize(5)
                .extracting(ContractAgreement::getContractAgreementId,
                        ContractAgreement::getContractDefinitionId,
                        ContractAgreement::getStatus)
                .contains(
                        tuple(contractId, definitionId, ContractAgreementStatusType.TERMINATED)
                );
    }

    @Test
    void shouldReturnBadRequestForWrongStatus(CapturedOutput output) {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2062");
        String definitionId = "807fdb6b-a9de-430d-9bb2-c5909b4b2064";
        StatusUpdateRequestTO status = new StatusUpdateRequestTO("CREATED");

        //when
        BadArgumentException exception = assertThrows(BadArgumentException.class,
                () -> contractAgreementController.updateContractAgreementStatus(contractId, definitionId, status));

        //then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldFailForBadStatus() {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2099");
        String definitionId = "807fdb6b-a9de-430d-9bb2-c5909b4b2064";
        StatusUpdateRequestTO status = new StatusUpdateRequestTO("NOSTATUSFOUND");

        //when
        BadArgumentException exception = assertThrows(BadArgumentException.class,
                () -> contractAgreementController.updateContractAgreementStatus(contractId, definitionId, status));

        //then
        assertEquals("400 BAD_REQUEST \"Status not recognized.\"", exception.getMessage());
    }

    @Test
    void shouldReturnDataForGet() {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2062");

        //when
        ResponseEntity<ContractAgreementTO> responseEntity = contractAgreementController.getContractAgreement(contractId);

        //then
        ContractAgreementTO contractAgreementTO = responseEntity.getBody();
        assertEquals(contractId, contractAgreementTO.getContractAgreementId());
        assertEquals(ContractAgreementStatusType.CREATED, contractAgreementTO.getStatus());
        assertEquals("X2", contractAgreementTO.getAssetId());
        assertEquals("X4", contractAgreementTO.getConsumerId());
        assertEquals("X3", contractAgreementTO.getProviderId());
        assertEquals("X1", contractAgreementTO.getContractNegotiationId());
        assertEquals("X5", contractAgreementTO.getContractOfferId());
        assertEquals("807fdb6b-a9de-430d-9bb2-c5909b4b2064", contractAgreementTO.getContractDefinitionId());
        assertNotNull(contractAgreementTO.getProviderSignatureDate());
        assertNotNull(contractAgreementTO.getConsumerSignatureDate());
    }

    @Test
    void shouldFailWhenRecordNotFound() {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2099");

        //when
        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class,
                () -> contractAgreementController.getContractAgreement(contractId));

        //then
        assertEquals("ContractAgreement not found for contractAgreementId = 807fdb6b-a9de-430d-9bb2-c5909b4b2099", exception.getReason());
    }

    @Test
    void shouldFailWhenRecordNotFoundForGetFile() {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2099");

        //when
        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class,
                () -> contractAgreementController.getContractAgreementFile(contractId));

        //then
        assertEquals("ContractAgreement not found for contractAgreementId = 807fdb6b-a9de-430d-9bb2-c5909b4b2099", exception.getReason());
    }

    @Test
    void shouldFailWhenRecordNotFinalizedForGetFile() {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2064");

        //when
        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class,
                () -> contractAgreementController.getContractAgreementFile(contractId));

        //then
        assertEquals("ContractAgreementFile not found for contractAgreementId = 807fdb6b-a9de-430d-9bb2-c5909b4b2064", exception.getReason());
    }

    @Test
    void shouldFailWhenFileNotExistsForGetFile(CapturedOutput output) {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2061");

        //when
        ResponseEntity<String> response = contractAgreementController.getContractAgreementFile(contractId);

        //then
        assertNull(response.getBody());
        await().until(() -> output.getOut().contains("File does not exist:"));
    }

    @Test
    void shouldProcessFullFlowForFinalized(CapturedOutput output) {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2062");
        String definitionId = "807fdb6b-a9de-430d-9bb2-c5909b4b2064";
        StatusUpdateRequestTO status = new StatusUpdateRequestTO("FINALIZED");

        //when
        ResponseEntity<ContractResponseTO> responseEntity = contractAgreementController.updateContractAgreementStatus(contractId, definitionId, status);

        //then
        assertEquals(contractId, responseEntity.getBody().getContractAgreementId());
        assertEquals(definitionId, responseEntity.getBody().getContractDefinitionId());
        assertEquals(ContractAgreementStatusType.FINALIZING, responseEntity.getBody().getStatus());
        await().until(() -> output.getOut().contains("Contract confirmed"));
        List<ContractAgreement> dbData = contractAgreementRepository.findAll();
        assertThat(dbData)
                .hasSize(5)
                .extracting(ContractAgreement::getContractAgreementId,
                        ContractAgreement::getContractDefinitionId,
                        ContractAgreement::getStatus)
                .contains(
                        tuple(contractId, definitionId, ContractAgreementStatusType.FINALIZED)
                );

        // when 2
        ResponseEntity<String> responseEntityFile = contractAgreementController.getContractAgreementFile(contractId);

        // then 2
        String responseFile = responseEntityFile.getBody();
        assertTrue(responseFile.contains("CONTRACT AGREEMENT: 807fdb6b-a9de-430d-9bb2-c5909b4b2062"));
        assertTrue(responseFile.contains("CONTRACT DEFINITION: 807fdb6b-a9de-430d-9bb2-c5909b4b2064"));
        assertTrue(responseFile.contains("ASSET: X2"));
        assertTrue(responseFile.contains("PROVIDER ID: X3"));
        assertTrue(responseFile.contains("PROVIDER SIGNATURE DATE: 2023-07-04 13:37:15"));
        assertTrue(responseFile.contains("CONSUMER: X4"));
        assertTrue(responseFile.contains("CONSUMER SIGNATURE DATE: 2023-07-04 13:38:15"));
    }

    @Test
    void testIsActiveContractPresentByMultipleParameters(CapturedOutput capturedOutput) {

        doNothing().when(edcConnector).notifyContractIsPresent(any(), anyBoolean(), any());

        var contractAgreementId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2062");
        var contractDefinitionId = "807fdb6b-a9de-430d-9bb2-c5909b4b2064";
        var assetId = "X2";
        var providerId = "X3";
        var consumerId = "X4";

        ResponseEntity<ContractSearchConfirmationTO> responseEntity = contractAgreementController
                .isActiveContractPresent(CONTRACT_NEGOTIATION_ID,
                        prepareContractSearchTO(contractAgreementId, contractDefinitionId, assetId, providerId, consumerId));

        var body = responseEntity.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals(contractAgreementId, body.getContractAgreementId(), "ContractAgreementID should match");
        assertNotNull(body.getContractSearchTO(), "ContractSearchTO should not be null");
        assertEquals(contractDefinitionId, body.getContractSearchTO().getContractDefinitionId(),
                "ContractDefinitionID should match");
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> capturedOutput.getOut()
                .contains("Contract search result (is present): true, for ContractNegotiationID: " + contractDefinitionId));
    }

    private @Valid ContractSearchTO prepareContractSearchTO(UUID contractAgreementId, String contractDefinitionId,
                                                            String assetId, String providerId, String consumerId) {

        var contractSearchTO = new ContractSearchTO();
        contractSearchTO.setContractAgreementId(contractAgreementId);
        contractSearchTO.setContractDefinitionId(contractDefinitionId);
        contractSearchTO.setAssetId(assetId);
        contractSearchTO.setProviderId(providerId);
        contractSearchTO.setConsumerId(consumerId);
        return contractSearchTO;
    }

    @Test
    void testIsActiveContractPresentBySomeNullParameters(CapturedOutput capturedOutput) {

        doNothing().when(edcConnector).notifyContractIsPresent(any(), anyBoolean(), any());

        var assetId = "X2";
        var providerId = "X3";
        var consumerId = "X4";

        ResponseEntity<ContractSearchConfirmationTO> responseEntity = contractAgreementController.isActiveContractPresent(
                CONTRACT_NEGOTIATION_ID,
                prepareContractSearchTO(null, null, assetId, providerId, consumerId));

        assertNotNull(responseEntity.getBody(), "Response body should not be null");
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> capturedOutput.getOut()
                .contains("Contract search result (is present): true"));
    }

    @Test
    void testIsActiveContractNotPresentBySomeNullParameters(CapturedOutput capturedOutput) {

        doNothing().when(edcConnector).notifyContractIsPresent(any(), anyBoolean(), any());

        var assetId = "X777";
        var providerId = "X3";
        var consumerId = "X4";

        ResponseEntity<ContractSearchConfirmationTO> responseEntity = contractAgreementController.isActiveContractPresent(
                CONTRACT_NEGOTIATION_ID,
                prepareContractSearchTO(null, null, assetId, providerId, consumerId));

        assertNotNull(responseEntity.getBody(), "Response body should not be null");
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> capturedOutput.getOut()
                .contains("Contract search result (is present): false"));
    }

    @Test
    void testIsActiveContractNotPresentWithAllNullParameters() {

        BadArgumentException exception = assertThrows(BadArgumentException.class,
                () -> contractAgreementController.isActiveContractPresent(CONTRACT_NEGOTIATION_ID, null),
                "Controller should throw BadArgumentException");

        assertEquals("400 BAD_REQUEST \"At least one search parameter must be provided correctly.\"", exception.getMessage(),
                "Exception Message not as expected");

        var cs = new ContractSearchTO();
        BadArgumentException exception2 = assertThrows(BadArgumentException.class,
                () -> contractAgreementController.isActiveContractPresent(CONTRACT_NEGOTIATION_ID, cs),
                "Controller should throw BadArgumentException");

        assertEquals("400 BAD_REQUEST \"At least one search parameter must be provided correctly.\"", exception2.getMessage(),
                "Exception Message not as expected");
    }
}
