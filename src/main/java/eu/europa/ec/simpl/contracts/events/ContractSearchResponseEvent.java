package eu.europa.ec.simpl.contracts.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europa.ec.simpl.contracts.transfer.ContractSearchTO;
import eu.europa.ec.simpl.contracts.transfer.Mode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@NoArgsConstructor
public class ContractSearchResponseEvent extends ContractEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String STRING_VALIDATION_REGEXP = "^(?U)[\\w:-]+$";

    @Valid
    @NotBlank(message = "contractNegotiationId cannot be blank")
    @Pattern(
            regexp = STRING_VALIDATION_REGEXP,
            message = "contractNegotiationId may contain only alphanumeric characters and colons"
    )
    private String contractNegotiationId;

    @Valid
    @NotNull
    private ContractSearchTO contractSearchTO;

    @JsonProperty("isPresent")
    private boolean isPresent;

    public ContractSearchResponseEvent(String contractNegotiationId, ContractSearchTO cs, boolean isPresent) {

        super(cs.getContractAgreementId(), Mode.PROVIDER);
        this.contractNegotiationId = contractNegotiationId;
        this.contractSearchTO = cs;
        this.isPresent = isPresent;
    }
}
