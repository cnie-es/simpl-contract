package eu.europa.ec.simpl.contracts.transfer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ContractSearchConfirmationTO {

    private UUID contractAgreementId;
    private ContractSearchTO contractSearchTO;
}
