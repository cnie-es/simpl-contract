package eu.europa.ec.simpl.contracts.controller;

import eu.europa.ec.simpl.common.exceptions.BadArgumentException;
import eu.europa.ec.simpl.contracts.service.ContractAgreementService;
import eu.europa.ec.simpl.contracts.service.PaymentApprovalService;
import eu.europa.ec.simpl.contracts.transfer.ContractAgreementTO;
import eu.europa.ec.simpl.contracts.transfer.ContractResponseTO;
import eu.europa.ec.simpl.contracts.transfer.ContractSearchConfirmationTO;
import eu.europa.ec.simpl.contracts.transfer.ContractSearchTO;
import eu.europa.ec.simpl.contracts.transfer.StatusUpdateRequestTO;
import eu.europa.ec.simpl.contracts.types.ContractAgreementStatusType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/contract/v1/agreements")
@Slf4j
public class ContractAgreementController {

    private final ContractAgreementService contractAgreementService;
    private final PaymentApprovalService paymentApprovalService;

    public ContractAgreementController(ContractAgreementService contractAgreementService,
                                       PaymentApprovalService paymentApprovalService) {
        this.contractAgreementService = contractAgreementService;
        this.paymentApprovalService = paymentApprovalService;
    }

    @PatchMapping(value = "/{contractAgreementId}/definitions/{contractDefinitionId}/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContractResponseTO> updateContractAgreementStatus(
            @PathVariable(value = "contractAgreementId") UUID contractAgreementId,
            @PathVariable(value = "contractDefinitionId") String contractDefinitionId,
            @Valid @RequestBody StatusUpdateRequestTO statusUpdateRequestTO) {
        final ContractAgreementStatusType statusType;
        try {
            statusType = ContractAgreementStatusType.valueOf(statusUpdateRequestTO.getStatus());
        } catch (IllegalArgumentException ex) {
            log.warn("Bad status requested: {}", statusUpdateRequestTO.getStatus(), ex);
            throw new BadArgumentException("Status not recognized.", ex);
        }
        if (statusType != ContractAgreementStatusType.FINALIZED
                && statusType != ContractAgreementStatusType.TERMINATED) {
            throw new BadArgumentException("Status is not allowed.");
        }
        return ResponseEntity.ok(contractAgreementService.sendStatusUpdateRequest(contractAgreementId,
                contractDefinitionId, statusType));
    }

    @GetMapping(value = "/{contractAgreementId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContractAgreementTO> getContractAgreement(
            @PathVariable(value = "contractAgreementId") UUID contractAgreementId) {
        return ResponseEntity.ok(contractAgreementService.getContractAgreement(contractAgreementId));
    }

    @GetMapping(value = "/{contractAgreementId}/file", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getContractAgreementFile(
            @PathVariable(value = "contractAgreementId") UUID contractAgreementId) {
        return ResponseEntity.ok(contractAgreementService.getContractAgreementFile(contractAgreementId));
    }

    @GetMapping(value = "/pending-payment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ContractAgreementTO>> listAgreementsAwaitingPayment() {
        return ResponseEntity.ok(paymentApprovalService.findAwaitingPayment());
    }

    @PostMapping(value = "/{contractAgreementId}/payment/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContractAgreementTO> confirmPayment(
            @PathVariable(value = "contractAgreementId") UUID contractAgreementId) {
        return ResponseEntity.ok(paymentApprovalService.confirmPayment(contractAgreementId));
    }

    @PostMapping(value = "/{contractAgreementId}/payment/reject", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContractAgreementTO> rejectPayment(
            @PathVariable(value = "contractAgreementId") UUID contractAgreementId) {
        return ResponseEntity.ok(paymentApprovalService.rejectPayment(contractAgreementId));
    }

    @Validated
    @PostMapping(value = "{contractNegotiationId}/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContractSearchConfirmationTO> isActiveContractPresent(
            @PathVariable(value = "contractNegotiationId") @Pattern(regexp = "^(?U)[\\w:-]+$")
            String contractNegotiationId,
            @Valid @RequestBody ContractSearchTO contractSearchTO) {

        if (Objects.isNull(contractSearchTO) || isAllNull(contractSearchTO)) {
            throw new BadArgumentException("At least one search parameter must be provided correctly.");
        }

        return ResponseEntity.ok(contractAgreementService.sendIsActiveContractRequest(contractNegotiationId,
                contractSearchTO));
    }

    private static boolean isAllNull(ContractSearchTO cs) {

        final var isContractAgreementNull = Objects.isNull(cs.getContractAgreementId());
        final var areStringsNullOrEmpty = Stream.of(cs.getContractDefinitionId(), cs.getAssetId(), cs.getProviderId(),
                        cs.getConsumerId()).filter(Objects::nonNull).allMatch(String::isBlank);
        return isContractAgreementNull && areStringsNullOrEmpty;
    }
}
