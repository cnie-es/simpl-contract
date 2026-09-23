package eu.europa.ec.simpl.contracts.repository;

import eu.europa.ec.simpl.contracts.entity.ContractAgreement;
import eu.europa.ec.simpl.contracts.types.ContractAgreementStatusType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ContractAgreementRepository extends JpaRepository<ContractAgreement, UUID> {

    List<ContractAgreement>
    findAllByContractAgreementIdAndStatusIsNot(UUID contractId, ContractAgreementStatusType status);

    List<ContractAgreement> findAllByStatus(ContractAgreementStatusType status);

    List<ContractAgreement> findAllByStatusAndPendingPaymentSinceBefore(
            ContractAgreementStatusType status, LocalDateTime threshold);
}
