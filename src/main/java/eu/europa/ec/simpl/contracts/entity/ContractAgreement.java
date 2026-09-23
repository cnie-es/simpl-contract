package eu.europa.ec.simpl.contracts.entity;


import eu.europa.ec.simpl.contracts.types.ContractAgreementStatusType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contract_agreements")
@Getter
@Setter
@NoArgsConstructor
public class ContractAgreement {

    @Id
    private UUID contractAgreementId;

    private String contractDefinitionId;

    private LocalDateTime consumerSignatureDate;

    private LocalDateTime providerSignatureDate;

    @Enumerated(EnumType.STRING)
    private ContractAgreementStatusType status;

    private LocalDateTime pendingPaymentSince;

    private String contractNegotiationId;

    private String assetId;

    private String providerId;

    private String consumerId;

    private String contractOfferId;
}
