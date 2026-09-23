package eu.europa.ec.simpl.contracts.transfer;

import eu.europa.ec.simpl.contracts.types.ContractAgreementStatusType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ContractResponseTO {

    private UUID contractAgreementId;
    private String contractDefinitionId;
    private ContractAgreementStatusType status;

}
