package eu.europa.ec.simpl.contracts.transfer;


import com.fasterxml.jackson.annotation.JsonFormat;
import eu.europa.ec.simpl.contracts.types.ContractAgreementStatusType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ContractAgreementTO {

    private UUID contractAgreementId;
    private String contractDefinitionId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime consumerSignatureDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime providerSignatureDate;
    private ContractAgreementStatusType status;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime pendingPaymentSince;
    private String contractNegotiationId;
    private String assetId;
    private String providerId;
    private String consumerId;
    private String contractOfferId;
}
