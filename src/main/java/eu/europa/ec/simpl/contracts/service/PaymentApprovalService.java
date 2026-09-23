package eu.europa.ec.simpl.contracts.service;

import eu.europa.ec.simpl.common.exceptions.BadArgumentException;
import eu.europa.ec.simpl.common.exceptions.RecordNotFoundException;
import eu.europa.ec.simpl.contracts.entity.ContractAgreement;
import eu.europa.ec.simpl.contracts.mapper.ContractAgreementMapper;
import eu.europa.ec.simpl.contracts.repository.ContractAgreementRepository;
import eu.europa.ec.simpl.contracts.transfer.ContractAgreementTO;
import eu.europa.ec.simpl.contracts.transfer.Mode;
import eu.europa.ec.simpl.contracts.types.ContractAgreementStatusType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Provider-side gate that holds the signing of <b>paid</b> offerings until a human confirms, through
 * the UI, that the consumer has paid by an external means. Free offerings are unaffected and keep
 * being signed automatically.
 *
 * <p>The whole behaviour is opt-in via {@code contract.payment-approval.enabled} and only applies on
 * the provider side. While held, the contract agreement stays in {@link ContractAgreementStatusType#PENDING_PAYMENT}
 * and the underlying EDC negotiation remains pending (no agreement, so the consumer cannot consume).</p>
 *
 * <p>A held agreement that is neither confirmed nor rejected is automatically rejected after a
 * configurable window ({@code contract.payment-approval.expiration-days}, default 7 days), so
 * negotiations are not left hanging forever.</p>
 */
@Service
@Slf4j
public class PaymentApprovalService {

    /** Price type used by the catalogue for offerings that are free of charge. */
    private static final String FREE_PRICE_TYPE = "free";

    private final ContractAgreementRepository contractAgreementRepository;
    private final ContractSigningService contractSigningService;

    @Value("${contract.payment-approval.enabled:false}")
    private boolean enabled;

    @Value("${contract.payment-approval.expiration-days:7}")
    private long expirationDays;

    @Value("${spring.mode}")
    private String mode;

    public PaymentApprovalService(ContractAgreementRepository contractAgreementRepository,
                                  ContractSigningService contractSigningService) {
        this.contractAgreementRepository = contractAgreementRepository;
        this.contractSigningService = contractSigningService;
    }

    /**
     * Whether the given offering must be held for an explicit payment confirmation. Only paid
     * offerings on the provider side are held, and only when the feature is enabled.
     *
     * <p>The price type travels in the sign request (resolved by the connector from the EDC asset),
     * so no call to the federated catalogue is needed. Fail-safe behaviour: a missing/blank price
     * type is treated as <b>paid</b>, so a paid asset is never released automatically by mistake.</p>
     *
     * @param priceType the offering price type as published in the catalogue ({@code "free"} means free)
     * @return {@code true} if signing must wait for a payment confirmation
     */
    public boolean shouldHoldForPayment(String priceType) {
        if (!enabled || !Mode.PROVIDER.name().equals(mode)) {
            return false;
        }
        return isPaidOffer(priceType);
    }

    private boolean isPaidOffer(String priceType) {
        if (priceType == null || priceType.isBlank()) {
            log.warn("No priceType in sign request; treating offer as PAID (fail-safe).");
            return true;
        }
        final boolean paid = !FREE_PRICE_TYPE.equalsIgnoreCase(priceType.trim());
        log.info("Offering priceType='{}' resolved as {}.", priceType, paid ? "PAID" : "FREE");
        return paid;
    }

    /**
     * Marks an already persisted agreement as awaiting payment confirmation.
     *
     * @param contractAgreementId the persisted contract agreement id
     */
    @Transactional
    public void markPendingPayment(UUID contractAgreementId) {
        final ContractAgreement entity = findById(contractAgreementId);
        entity.setStatus(ContractAgreementStatusType.PENDING_PAYMENT);
        entity.setPendingPaymentSince(LocalDateTime.now());
        contractAgreementRepository.save(entity);
    }

    /**
     * Confirms that the payment has been received for a held agreement and releases the negotiation
     * by signing and notifying the connector.
     *
     * @param contractAgreementId the agreement awaiting payment
     * @return the updated agreement
     */
    @Transactional
    public ContractAgreementTO confirmPayment(UUID contractAgreementId) {
        final ContractAgreement entity = findPending(contractAgreementId);
        log.info("Payment confirmed for contract agreement {} (negotiation {}); signing.",
                contractAgreementId, entity.getContractNegotiationId());

        contractSigningService.signAndPublishResponse(entity.getContractAgreementId(),
                entity.getContractNegotiationId());

        return ContractAgreementMapper.INSTANCE.mapToTO(
                contractAgreementRepository.findById(contractAgreementId).orElse(entity));
    }

    /**
     * Rejects a held agreement (payment not received / refused) and terminates the negotiation.
     *
     * @param contractAgreementId the agreement awaiting payment
     * @return the updated agreement
     */
    @Transactional
    public ContractAgreementTO rejectPayment(UUID contractAgreementId) {
        final ContractAgreement entity = findPending(contractAgreementId);
        log.info("Payment rejected for contract agreement {} (negotiation {}); terminating.",
                contractAgreementId, entity.getContractNegotiationId());

        terminate(entity);
        return ContractAgreementMapper.INSTANCE.mapToTO(entity);
    }

    /**
     * Lists the agreements currently awaiting a payment confirmation, for display in the provider UI.
     *
     * @return agreements in {@link ContractAgreementStatusType#PENDING_PAYMENT}
     */
    @Transactional(readOnly = true)
    public List<ContractAgreementTO> findAwaitingPayment() {
        return contractAgreementRepository.findAllByStatus(ContractAgreementStatusType.PENDING_PAYMENT)
                .stream()
                .map(ContractAgreementMapper.INSTANCE::mapToTO)
                .toList();
    }

    /**
     * Periodically rejects held agreements whose payment confirmation window has elapsed, so pending
     * negotiations are not left open indefinitely.
     */
    @Scheduled(fixedDelayString = "${contract.payment-approval.check-interval-ms:3600000}")
    @Transactional
    public void expireStalePendingPayments() {
        if (!enabled || !Mode.PROVIDER.name().equals(mode)) {
            return;
        }
        final LocalDateTime threshold = LocalDateTime.now().minusDays(expirationDays);
        final List<ContractAgreement> expired = contractAgreementRepository
                .findAllByStatusAndPendingPaymentSinceBefore(ContractAgreementStatusType.PENDING_PAYMENT, threshold);

        if (expired.isEmpty()) {
            return;
        }
        log.info("Expiring {} contract agreement(s) whose {}-day payment window elapsed.",
                expired.size(), expirationDays);
        for (final ContractAgreement entity : expired) {
            log.info("Payment confirmation window expired for contract agreement {} (negotiation {}); terminating.",
                    entity.getContractAgreementId(), entity.getContractNegotiationId());
            terminate(entity);
        }
    }

    private void terminate(ContractAgreement entity) {
        contractSigningService.publishRejection(entity.getContractAgreementId(), entity.getContractNegotiationId());
        entity.setStatus(ContractAgreementStatusType.TERMINATED);
        contractAgreementRepository.save(entity);
    }

    private ContractAgreement findPending(UUID contractAgreementId) {
        final ContractAgreement entity = findById(contractAgreementId);
        if (entity.getStatus() != ContractAgreementStatusType.PENDING_PAYMENT) {
            throw new BadArgumentException("Contract agreement " + contractAgreementId
                    + " is not awaiting payment confirmation (status: " + entity.getStatus() + ").");
        }
        return entity;
    }

    private ContractAgreement findById(UUID contractAgreementId) {
        return contractAgreementRepository.findById(contractAgreementId)
                .orElseThrow(() -> new RecordNotFoundException(
                        "ContractAgreement", "contractAgreementId", contractAgreementId.toString()));
    }
}
