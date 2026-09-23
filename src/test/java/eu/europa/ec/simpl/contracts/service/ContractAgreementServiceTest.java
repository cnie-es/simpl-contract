package eu.europa.ec.simpl.contracts.service;

import eu.europa.ec.simpl.common.exceptions.BadArgumentException;
import eu.europa.ec.simpl.contracts.entity.ContractAgreement;
import eu.europa.ec.simpl.contracts.producer.MessageProducer;
import eu.europa.ec.simpl.contracts.repository.ContractAgreementRepository;
import eu.europa.ec.simpl.contracts.transfer.ContractSearchTO;
import eu.europa.ec.simpl.contracts.types.ContractAgreementStatusType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Sql("/database/ContractAgreementServiceConfirmationData.sql")
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
class ContractAgreementServiceTest {

    @MockitoBean
    private MessageProducer messageProducer;

    @Autowired
    @InjectMocks
    private ContractAgreementService contractAgreementService;

    @Autowired
    private ContractAgreementRepository contractAgreementRepository;

    @Test
    void shouldUpdateContractAgreementStatus() {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2062");
        String definitionId = "807fdb6b-a9de-430d-9bb2-c5909b4b2064";

        //when
        contractAgreementService.updateContractStatus(contractId, definitionId, ContractAgreementStatusType.FINALIZED);

        //then
        List<ContractAgreement> dbData = contractAgreementRepository.findAll();
        assertThat(dbData)
                .hasSize(5)
                .extracting(ContractAgreement::getContractAgreementId,
                        ContractAgreement::getContractDefinitionId,
                        ContractAgreement::getStatus)
                .contains(
                        tuple(contractId, definitionId, ContractAgreementStatusType.FINALIZED)
                );
        ContractAgreement contractAgreement = contractAgreementRepository.findById(contractId).get();
        assertNotNull(contractAgreement.getConsumerSignatureDate());
        assertNotNull(contractAgreement.getProviderSignatureDate());
    }

    @Test
    void shouldFailForNotMatchingDefinitionId() {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2063");
        String definitionId = "807fdb6b-a9de-430d-9bb2-c5909b4b2067";

        // when
        BadArgumentException exception = assertThrows(BadArgumentException.class,
                () -> contractAgreementService.updateContractStatus(contractId, definitionId, ContractAgreementStatusType.FINALIZED));

        // then
        assertEquals("ContractDefinitionId is not the same: 807fdb6b-a9de-430d-9bb2-c5909b4b2064" +
                " 807fdb6b-a9de-430d-9bb2-c5909b4b2067", exception.getReason());
    }

    @Test
    void shouldFailBecauseAlreadyFinalized() {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2061");
        String definitionId = "807fdb6b-a9de-430d-9bb2-c5909b4b2064";

        // when
        BadArgumentException exception = assertThrows(BadArgumentException.class,
                () -> contractAgreementService.updateContractStatus(contractId, definitionId, ContractAgreementStatusType.FINALIZED));

        // then
        assertEquals("Contract already finalized", exception.getReason());
    }

    @Test
    void shouldFailIfContractAgreementNotExists(CapturedOutput output) {
        //given
        UUID contractId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2065");
        String definitionId = "807fdb6b-a9de-430d-9bb2-c5909b4b2064";

        // when
        contractAgreementService.updateContractStatus(contractId, definitionId, ContractAgreementStatusType.FINALIZED);

        // then
        await().until(() -> output.getOut().contains("ContractAgreement not found for contractAgreementId: 807fdb6b-a9de-430d-9bb2-c5909b4b2065"));
    }

    @Test
    void testIsActiveContractPresentByMultipleParametersWhenTerminated() {

        //given: terminated contract present in database
        var contractAgreementId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2777");
        var contractDefinitionId = "807fdb6b-a9de-430d-9bb2-c5909b4b2777";
        var contractSearchTO = prepareContractSearchTO(contractAgreementId, contractDefinitionId, null, null, null);

        //when: isActiveContract is invoked
        var result = contractAgreementService.isActiveContractPresent(contractSearchTO);
        //then: terminated contracts should not be taken into consideration
        assertFalse(result, "Response should be FALSE");
    }

    @Test
    void testIsActiveContractPresentByMultipleParameters() {

        var contractAgreementId = UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2062");
        var contractDefinitionId = "807fdb6b-a9de-430d-9bb2-c5909b4b2064";
        var assetId = "X2";
        var providerId = "X3";
        var consumerId = "X4";
        var contractSearchTO = prepareContractSearchTO(contractAgreementId, contractDefinitionId, assetId,
                providerId, consumerId);

        var result = contractAgreementService.isActiveContractPresent(contractSearchTO);
        assertTrue(result, "Response should be TRUE");
    }

    private ContractSearchTO prepareContractSearchTO(UUID contractAgreementId, String contractDefinitionId,
                                                     String assetId, String providerId, String consumerId) {

        var cs = new ContractSearchTO();
        cs.setContractAgreementId(contractAgreementId);
        cs.setContractDefinitionId(contractDefinitionId);
        cs.setAssetId(assetId);
        cs.setProviderId(providerId);
        cs.setConsumerId(consumerId);
        return cs;
    }

    @Test
    void testIsActiveContractPresentBySomeNullParameters() {

        var assetId = "X2";
        var providerId = "X3";
        var consumerId = "X4";
        var contractSearchTO = prepareContractSearchTO(null, null, assetId, providerId, consumerId);

        var result = contractAgreementService.isActiveContractPresent(contractSearchTO);
        assertTrue(result, "Response should be TRUE");
    }

    @Test
    void testIsActiveContractNotPresentBySomeNullParameters() {

        var assetId = "X777";
        var providerId = "X3";
        var consumerId = "X4";
        var contractSearchTO = prepareContractSearchTO(null, null, assetId, providerId, consumerId);

        var result = contractAgreementService.isActiveContractPresent(contractSearchTO);
        assertFalse(result, "Response should be FALSE");
    }

    @Test
    void testSendIsActiveContractRequest() {

        var contractSearchTO = new ContractSearchTO();
        var contractNegotiationId = "807fdb6b-a9de-430d-9bb2-c5909b4b2064";
        contractSearchTO.setContractAgreementId(UUID.fromString("807fdb6b-a9de-430d-9bb2-c5909b4b2062"));
        contractSearchTO.setContractDefinitionId("807fdb6b-a9de-430d-9bb2-c5909b4b2064");
        contractSearchTO.setAssetId("X777");
        contractSearchTO.setProviderId("X3");
        contractSearchTO.setConsumerId("X4");

        when(messageProducer.sendMessage(any(), any())).thenReturn(true);
        var result = contractAgreementService.sendIsActiveContractRequest(contractNegotiationId, contractSearchTO);

        assertNotNull(result, "Response is null");
        assertEquals(contractSearchTO.getContractDefinitionId(), result.getContractSearchTO().getContractDefinitionId(),
                "Contract definition ID is not correct");
        assertEquals(contractSearchTO.getContractAgreementId(), result.getContractAgreementId(),
                "Agreement ID is not correct");
    }
}
