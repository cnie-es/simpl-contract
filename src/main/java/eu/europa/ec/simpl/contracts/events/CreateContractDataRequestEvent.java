package eu.europa.ec.simpl.contracts.events;

import eu.europa.ec.simpl.contracts.transfer.ContractAgreementCreateTO;
import eu.europa.ec.simpl.contracts.transfer.Mode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CreateContractDataRequestEvent extends ContractEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    @Valid
    @NotBlank(message = "contractDefinitionId cannot be blank")
    @Pattern(
            regexp = "^(?U)[\\w:-]+$",
            message = "contractDefinitionId may contain only alphanumeric characters and colons"
    )
    private String contractDefinitionId;

    @Valid
    @NotNull
    private ContractAgreementCreateTO contractAgreementCreateTO;

    public CreateContractDataRequestEvent(UUID contractAgreementId, String contractDefinitionId,
                                          Mode mode, ContractAgreementCreateTO contractAgreementCreateTO) {

        super(contractAgreementId, mode);
        this.contractAgreementCreateTO = contractAgreementCreateTO.copyOf();
        this.contractDefinitionId = contractDefinitionId;
    }
}
