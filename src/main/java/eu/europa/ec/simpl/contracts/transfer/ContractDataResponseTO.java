package eu.europa.ec.simpl.contracts.transfer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContractDataResponseTO {

    private String response;

    private String humanReadable;

    private String hash;

    private String contractNegotiationId;

    private String errorMessage;

    private int errorCode;
}