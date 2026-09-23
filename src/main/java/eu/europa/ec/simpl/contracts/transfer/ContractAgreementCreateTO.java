package eu.europa.ec.simpl.contracts.transfer;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class ContractAgreementCreateTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private String contractNegotiationId;
    private String assetId;
    private String providerId;
    private String consumerId;
    private String contractOfferId;
    // Offering price type ("free" / "commercial") resolved from the EDC asset by the connector and sent
    // by the provider, so the payment-approval gate does not need to query the federated catalogue.
    private String priceType;

    public ContractAgreementCreateTO copyOf() {
        final ContractAgreementCreateTO newObject = new ContractAgreementCreateTO();
        newObject.contractNegotiationId = this.contractNegotiationId;
        newObject.assetId = this.assetId;
        newObject.providerId = this.providerId;
        newObject.consumerId = this.consumerId;
        newObject.contractOfferId = this.contractOfferId;
        newObject.priceType = this.priceType;
        return newObject;
    }

}
