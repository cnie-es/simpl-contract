package eu.europa.ec.simpl.contracts.events;

import eu.europa.ec.simpl.contracts.transfer.Mode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CreateContractDataResponseEvent extends ContractEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    @Valid
    private String contractNegotiationId;

    @Valid
    @NotNull
    private String responseData;

    public CreateContractDataResponseEvent(UUID contractAgreementId, String contractNegotiationId,
                                           Mode mode, String responseData) {

        super(contractAgreementId, mode);
        this.responseData = responseData;
        this.contractNegotiationId = contractNegotiationId;
    }
}
