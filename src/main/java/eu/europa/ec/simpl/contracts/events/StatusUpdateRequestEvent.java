package eu.europa.ec.simpl.contracts.events;

import eu.europa.ec.simpl.contracts.transfer.Mode;
import eu.europa.ec.simpl.contracts.types.ContractAgreementStatusType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateRequestEvent extends ContractEvent {

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
    private ContractAgreementStatusType newStatus;

    public StatusUpdateRequestEvent(UUID contractAgreementId, String contractDefinitionId,
                                    Mode mode, ContractAgreementStatusType status) {

        super(contractAgreementId, mode);
        this.newStatus = status;
        this.contractDefinitionId = contractDefinitionId;
    }
}
