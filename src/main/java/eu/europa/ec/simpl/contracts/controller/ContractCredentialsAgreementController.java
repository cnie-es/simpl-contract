package eu.europa.ec.simpl.contracts.controller;

import eu.europa.ec.simpl.contracts.service.ContractAgreementService;
import eu.europa.ec.simpl.contracts.transfer.ContractAgreementCreateTO;
import eu.europa.ec.simpl.contracts.transfer.ContractResponseTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/contract/v1/credentials")
public class ContractCredentialsAgreementController {

    private final ContractAgreementService contractAgreementService;

    public ContractCredentialsAgreementController(ContractAgreementService contractAgreementService) {
        this.contractAgreementService = contractAgreementService;
    }

    /**
     * This method is deprecated and will be removed (replaced by /issue)
     */
    @PostMapping("/agreements/{contractAgreementId}/definitions/{contractDefinitionId}")
    public ResponseEntity<ContractResponseTO> issueVerifiableCredentialDeprecated(
            @PathVariable(value = "contractAgreementId") UUID contractAgreementId,
            @PathVariable(value = "contractDefinitionId") String contractDefinitionId,
            @Valid @RequestBody ContractAgreementCreateTO contractAgreementCreateTO) {
        return ResponseEntity.ok(contractAgreementService.issueVerifiableCredential(contractAgreementId,
                contractDefinitionId, contractAgreementCreateTO));
    }

    @PostMapping("/agreements/{contractAgreementId}/definitions/{contractDefinitionId}/issue")
    public ResponseEntity<ContractResponseTO> issueVerifiableCredential(
            @PathVariable(value = "contractAgreementId") UUID contractAgreementId,
            @PathVariable(value = "contractDefinitionId") String contractDefinitionId,
            @Valid @RequestBody ContractAgreementCreateTO contractAgreementCreateTO) {
        return ResponseEntity.status(HttpStatusCode.valueOf(HttpStatus.CREATED.value()))
                .body(contractAgreementService.issueVerifiableCredential(contractAgreementId,
                contractDefinitionId, contractAgreementCreateTO));
    }

    @PostMapping("/agreements/{contractAgreementId}/definitions/{contractDefinitionId}/create")
    public ResponseEntity<ContractResponseTO> createVerifiableCredential(
            @PathVariable(value = "contractAgreementId") UUID contractAgreementId,
            @PathVariable(value = "contractDefinitionId") String contractDefinitionId,
            @Valid @RequestBody ContractAgreementCreateTO contractAgreementCreateTO) {
        return ResponseEntity.status(HttpStatusCode.valueOf(HttpStatus.CREATED.value()))
                .body(contractAgreementService.createVerifiableCredential(contractAgreementId,
                contractDefinitionId, contractAgreementCreateTO));
    }
}
