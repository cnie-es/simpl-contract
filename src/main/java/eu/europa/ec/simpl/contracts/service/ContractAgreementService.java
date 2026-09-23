package eu.europa.ec.simpl.contracts.service;

import eu.europa.ec.simpl.common.exceptions.BadArgumentException;
import eu.europa.ec.simpl.common.exceptions.RecordNotFoundException;
import eu.europa.ec.simpl.contracts.config.BusinessOperationsLogger;
import eu.europa.ec.simpl.contracts.entity.ContractAgreement;
import eu.europa.ec.simpl.contracts.events.ContractAgreementRequestEvent;
import eu.europa.ec.simpl.contracts.events.ContractSearchRequestEvent;
import eu.europa.ec.simpl.contracts.events.CreateContractDataRequestEvent;
import eu.europa.ec.simpl.contracts.events.StatusUpdateRequestEvent;
import eu.europa.ec.simpl.contracts.kafka.KafkaTopic;
import eu.europa.ec.simpl.contracts.mapper.ContractAgreementMapper;
import eu.europa.ec.simpl.contracts.mapper.ContractMapper;
import eu.europa.ec.simpl.contracts.producer.MessageProducer;
import eu.europa.ec.simpl.contracts.repository.ContractAgreementRepository;
import eu.europa.ec.simpl.contracts.transfer.ContractAgreementCreateTO;
import eu.europa.ec.simpl.contracts.transfer.ContractAgreementTO;
import eu.europa.ec.simpl.contracts.transfer.ContractResponseTO;
import eu.europa.ec.simpl.contracts.transfer.ContractSearchConfirmationTO;
import eu.europa.ec.simpl.contracts.transfer.ContractSearchTO;
import eu.europa.ec.simpl.contracts.transfer.Mode;
import eu.europa.ec.simpl.contracts.types.BusinessOperations;
import eu.europa.ec.simpl.contracts.types.ContractAgreementStatusType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static eu.europa.ec.simpl.contracts.kafka.KafkaTopic.CREATE_CONTRACT_REQUEST;
import static eu.europa.ec.simpl.contracts.kafka.KafkaTopic.SIGN_CONTRACT_REQUEST;
import static eu.europa.ec.simpl.common_logging.types.MessageType.RESPONSE;

@Service
@ComponentScan("eu.europa.ec.simpl.common.events")
@Slf4j
public class ContractAgreementService {

    public static final String CONTRACT_ID = "contractAgreementId";
    private final ContractAgreementRepository contractAgreementRepository;
    private final MessageProducer messageProducer;
    private final ContractAgreementFileStorageService contractAgreementFileStorageService;

    @Value("${spring.mode}")
    private String mode;

    public ContractAgreementService(ContractAgreementRepository contractAgreementRepository,
                                    MessageProducer messageProducer,
                                    ContractAgreementFileStorageService contractAgreementFileStorageService) {
        this.contractAgreementRepository = contractAgreementRepository;
        this.messageProducer = messageProducer;
        this.contractAgreementFileStorageService = contractAgreementFileStorageService;
    }

    @Transactional
    public void updateContractStatus(UUID contractAgreementId, String contractDefinitionId,
                                     ContractAgreementStatusType status) {
        Optional<ContractAgreement> contractAgreementOpt = contractAgreementRepository.findById(contractAgreementId);
        if (contractAgreementOpt.isPresent()) {
            ContractAgreement contractAgreement = contractAgreementOpt.get();
            validateContractAgreement(contractDefinitionId, contractAgreement);
            contractAgreement.setStatus(status);
            contractAgreementRepository.save(contractAgreement);
            contractAgreementRepository.flush();
            if (status == ContractAgreementStatusType.FINALIZED) {
                contractAgreementFileStorageService.storeFileForContractAgreement(contractAgreement);
                BusinessOperationsLogger.log(BusinessOperations.BP07_09, RESPONSE,
                        contractAgreementId, "Contract confirmed for ID: ");
            } else {
                BusinessOperationsLogger.log(BusinessOperations.BP07_06, RESPONSE,
                        contractAgreementId, "Contract terminated for ID: ");
            }
        } else {
            log.info("ContractAgreement not found for contractAgreementId: {}", contractAgreementId);
        }
    }

    @Transactional
    public void createAndSaveContractAgreement(UUID contractAgreementId, String contractDefinitionId,
                                               ContractAgreementCreateTO contractAgreementCreateTO) {
        ContractAgreement contractAgreement = new ContractAgreement();
        contractAgreement.setContractAgreementId(contractAgreementId);
        contractAgreement.setContractDefinitionId(contractDefinitionId);
        contractAgreement.setStatus(ContractAgreementStatusType.INITIATED);
        contractAgreement.setConsumerSignatureDate(LocalDateTime.now());
        if (contractAgreementCreateTO != null) {
            contractAgreement.setContractNegotiationId(contractAgreementCreateTO.getContractNegotiationId());
            contractAgreement.setContractOfferId(contractAgreementCreateTO.getContractOfferId());
            contractAgreement.setAssetId(contractAgreementCreateTO.getAssetId());
            contractAgreement.setProviderId(contractAgreementCreateTO.getProviderId());
            contractAgreement.setConsumerId(contractAgreementCreateTO.getConsumerId());
        }
        contractAgreementRepository.save(contractAgreement);
    }

    @Transactional
    public void updateContractAgreementProviderDateAndStatus(UUID contractAgreementId) {
        ContractAgreement contractAgreement = contractAgreementRepository.findById(contractAgreementId).get();
        contractAgreement.setStatus(ContractAgreementStatusType.CREATED);
        contractAgreement.setProviderSignatureDate(LocalDateTime.now());
        contractAgreementRepository.save(contractAgreement);
    }

    public ContractResponseTO sendStatusUpdateRequest(UUID contractAgreementId, String contractDefinitionId,
                                                      ContractAgreementStatusType status) {
        final StatusUpdateRequestEvent event = new StatusUpdateRequestEvent(contractAgreementId,
                contractDefinitionId, Mode.valueOf(mode), status);
        messageProducer.sendMessage(KafkaTopic.STATUS_UPDATE, event);
        return ContractMapper.INSTANCE.mapToTO(event, ContractAgreementStatusType.FINALIZING);
    }

    public ContractResponseTO issueVerifiableCredential(UUID contractAgreementId, String contractDefinitionId,
                                                        ContractAgreementCreateTO contractAgreementCreateTO) {
        final ContractAgreementRequestEvent event = new ContractAgreementRequestEvent(contractAgreementId,
                contractDefinitionId, Mode.valueOf(mode), contractAgreementCreateTO);
        messageProducer.sendMessage(SIGN_CONTRACT_REQUEST, event);
        return ContractMapper.INSTANCE.mapToTO(event, ContractAgreementStatusType.INITIATED);
    }

    public ContractResponseTO createVerifiableCredential(UUID contractAgreementId, String contractDefinitionId,
                                                         ContractAgreementCreateTO contractAgreementCreateTO) {
        final CreateContractDataRequestEvent event = new CreateContractDataRequestEvent(contractAgreementId,
                contractDefinitionId, Mode.valueOf(mode), contractAgreementCreateTO);
        messageProducer.sendMessage(CREATE_CONTRACT_REQUEST, event);
        return ContractMapper.INSTANCE.mapToTO(event, ContractAgreementStatusType.INITIATED);
    }

    @Transactional(readOnly = true)
    public ContractAgreementTO getContractAgreement(UUID contractAgreementId) {
        ContractAgreement contractAgreement = contractAgreementRepository.findById(contractAgreementId).orElseThrow(
                () -> new RecordNotFoundException("ContractAgreement", CONTRACT_ID, contractAgreementId.toString())
        );
        return ContractAgreementMapper.INSTANCE.mapToTO(contractAgreement);
    }

    @Transactional(readOnly = true)
    public String getContractAgreementFile(UUID contractAgreementId) {
        ContractAgreement contractAgreement = contractAgreementRepository.findById(contractAgreementId).orElseThrow(
                () -> new RecordNotFoundException("ContractAgreement", CONTRACT_ID, contractAgreementId.toString())
        );
        if (contractAgreement.getStatus() != ContractAgreementStatusType.FINALIZED) {
            throw new RecordNotFoundException("ContractAgreementFile", CONTRACT_ID, contractAgreementId.toString());
        }
        return contractAgreementFileStorageService.readFile(contractAgreementId);
    }

    private static void validateContractAgreement(String contractDefinitionId, ContractAgreement contractAgreement) {
        if (!contractDefinitionId.equals(contractAgreement.getContractDefinitionId())) {
            throw new BadArgumentException("ContractDefinitionId is not the same: " +
                    contractAgreement.getContractDefinitionId() + " " + contractDefinitionId);
        }
        if (contractAgreement.getStatus() == ContractAgreementStatusType.FINALIZED
                || contractAgreement.getStatus() == ContractAgreementStatusType.TERMINATED) {
            throw new BadArgumentException("Contract already finalized");
        }
    }

    @Transactional(readOnly = true)
    public Boolean isActiveContractPresent(ContractSearchTO cs) {

        final var contractAgreement = new ContractAgreement();
        contractAgreement.setContractAgreementId(cs.getContractAgreementId());
        contractAgreement.setContractDefinitionId(cs.getContractDefinitionId());
        contractAgreement.setAssetId(cs.getAssetId());
        contractAgreement.setProviderId(cs.getProviderId());
        contractAgreement.setConsumerId(cs.getConsumerId());

        final var matcher = ExampleMatcher.matchingAll().withIgnoreCase().withIgnoreNullValues();
        final Example<ContractAgreement> example = Example.of(contractAgreement, matcher);
        final var result = contractAgreementRepository.findAll(example).stream().
                filter(ca -> ContractAgreementStatusType.TERMINATED != ca.getStatus()).count();
        return result > 0;
    }

    public ContractSearchConfirmationTO sendIsActiveContractRequest(String contractNegotiationId, ContractSearchTO cs) {

        final var event = new ContractSearchRequestEvent(contractNegotiationId, cs);
        messageProducer.sendMessage(KafkaTopic.SEARCH_ACTIVE_CONTRACT_REQUEST, event);
        return ContractMapper.INSTANCE.mapToTO(event);
    }
}
