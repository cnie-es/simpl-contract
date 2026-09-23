package eu.europa.ec.simpl.contracts.events;

import eu.europa.ec.simpl.contracts.transfer.Mode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContractAgreementResponseEvent extends ContractEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    @Valid
    private String contractNegotiationId;

    @Valid
    @NotNull
    private boolean signed;

    public ContractAgreementResponseEvent(UUID contractAgreementId, String contractNegotiationId,
                                          Mode mode, boolean signed) {

        super(contractAgreementId, mode);
        this.signed = signed;
        this.contractNegotiationId = contractNegotiationId;
    }
}
