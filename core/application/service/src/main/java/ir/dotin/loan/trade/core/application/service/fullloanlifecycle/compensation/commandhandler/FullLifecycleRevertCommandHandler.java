package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.compensation.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.dispatcher.api.command.CommandHandler;
import ir.dotin.platform.pangaea.saga.api.error.SagaErrors;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLifecycleRevertCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FullLifecycleRevertCommandHandler implements CommandHandler<FullLifecycleRevertCommand> {

    private final TradeLoanFacilityRepository repository;
    private final TransactionPostingPort transactionPostingPort;
    private final CollateralServicePort collateralServicePort;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(FullLifecycleRevertCommand command) {
        log.info("Starting full lifecycle revert for facility: {}", command.loanFacilityId());

        return loadFacility(command.loanFacilityId()).flatMap(facility -> executeLifoRevert(facility, command));
    }

    private Result<TradeLoanFacility> loadFacility(java.util.UUID facilityId) {
        return Result.fromOptional(
                repository.findById(LoanFacilityId.of(facilityId)),
                () -> FailureCause.businessRule(
                        Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, facilityId)));
    }

    private Result<List<DomainEvent<?>>> executeLifoRevert(
            TradeLoanFacility facility, FullLifecycleRevertCommand command) {

        FacilityStatus currentStatus = facility.getCurrentState();
        log.info(
                "Facility {} is in state: {}, starting LIFO revert",
                facility.getId().value(),
                currentStatus);

        List<DomainEvent<?>> collectedEvents = new ArrayList<>();

        return switch (currentStatus) {
            case FULLY_DISBURSED -> revertFromFullyDisbursed(facility, command, collectedEvents);
            case PARTIALLY_DISBURSED -> revertFromPartiallyDisbursed(facility, command, collectedEvents);
            case ISSUE_CONTRACT -> revertFromContractIssued(facility, command, collectedEvents);
            case APPROVED -> revertFromApproved(facility, command, collectedEvents);
            case APPROVAL_SUBMITTED -> revertFromApprovalSubmitted(facility, command, collectedEvents);
            case APPLICATION_SUBMITTED -> revertFromOrigination(facility, command, collectedEvents);
            default ->
                Result.failure(Notification.ofError(
                        SagaErrors.INVALID_STATE_FOR_COMPENSATION,
                        facility.getId().value(),
                        currentStatus));
        };
    }

    private Result<List<DomainEvent<?>>> revertFromFullyDisbursed(
            TradeLoanFacility facility, FullLifecycleRevertCommand command, List<DomainEvent<?>> events) {

        log.info(
                "Reverting from FULLY_DISBURSED for facility: {}",
                facility.getId().value());

        if (command.disbursementTransactionNumberToReverse() != null) {
            reverseTransaction(command.disbursementTransactionNumberToReverse());
        }

        return facility.revertLumpSumDisbursement(clock)
                .onSuccess(v -> {
                    repository.save(facility);
                    events.addAll(facility.domainEvents());
                    facility.clearDomainEvents();
                })
                .flatMap(v -> revertFromContractIssued(facility, command, events));
    }

    private Result<List<DomainEvent<?>>> revertFromPartiallyDisbursed(
            TradeLoanFacility facility, FullLifecycleRevertCommand command, List<DomainEvent<?>> events) {

        log.info(
                "Reverting from PARTIALLY_DISBURSED for facility: {}",
                facility.getId().value());

        if (command.disbursementTransactionNumberToReverse() != null) {
            reverseTransaction(command.disbursementTransactionNumberToReverse());
        }

        return facility.revertIrregularTrancheDisbursement(clock)
                .onSuccess(v -> {
                    repository.save(facility);
                    events.addAll(facility.domainEvents());
                    facility.clearDomainEvents();
                })
                .flatMap(v -> {
                    FacilityStatus newState = facility.getCurrentState();
                    if (newState == FacilityStatus.ISSUE_CONTRACT) {
                        return revertFromContractIssued(facility, command, events);
                    }
                    return revertFromPartiallyDisbursed(facility, command, events);
                });
    }

    private Result<List<DomainEvent<?>>> revertFromContractIssued(
            TradeLoanFacility facility, FullLifecycleRevertCommand command, List<DomainEvent<?>> events) {

        log.info(
                "Reverting from ISSUE_CONTRACT for facility: {}",
                facility.getId().value());

        if (command.contractTransactionNumberToReverse() != null) {
            reverseTransaction(command.contractTransactionNumberToReverse());
        }

        return facility.revertContractIssuance(clock)
                .onSuccess(v -> {
                    repository.save(facility);
                    events.addAll(facility.domainEvents());
                    facility.clearDomainEvents();
                })
                .flatMap(v -> revertFromApproved(facility, command, events));
    }

    private Result<List<DomainEvent<?>>> revertFromApproved(
            TradeLoanFacility facility, FullLifecycleRevertCommand command, List<DomainEvent<?>> events) {

        log.info("Reverting from APPROVED for facility: {}", facility.getId().value());

        if (!facility.getCollaterals().isEmpty()) {
            revertCollaterals(facility, command.uid(), command.collateralSerialsToRevert(), events);
        }

        return facility.revertApproval(clock)
                .onSuccess(v -> {
                    repository.save(facility);
                    events.addAll(facility.domainEvents());
                    facility.clearDomainEvents();
                })
                .flatMap(v -> revertFromApprovalSubmitted(facility, command, events));
    }

    private Result<List<DomainEvent<?>>> revertFromApprovalSubmitted(
            TradeLoanFacility facility, FullLifecycleRevertCommand command, List<DomainEvent<?>> events) {

        log.info(
                "Reverting from APPROVAL_SUBMITTED for facility: {}",
                facility.getId().value());

        return facility.revertApprovalSubmission(clock)
                .onSuccess(v -> {
                    repository.save(facility);
                    events.addAll(facility.domainEvents());
                    facility.clearDomainEvents();
                })
                .flatMap(v -> revertFromOrigination(facility, command, events));
    }

    private Result<List<DomainEvent<?>>> revertFromOrigination(
            TradeLoanFacility facility, FullLifecycleRevertCommand command, List<DomainEvent<?>> events) {

        log.info("Reverting origination for facility: {}", facility.getId().value());

        String reason = command.reason() != null ? command.reason() : "Full lifecycle revert";

        return facility.revertOrigination(clock, reason)
                .onSuccess(v -> {
                    repository.save(facility);
                    events.addAll(facility.domainEvents());
                })
                .map(v -> events);
    }

    private void revertCollaterals(
            TradeLoanFacility facility, UUID requestId, List<String> serialsToRevert, List<DomainEvent<?>> events) {

        List<Collateral> collateralsToRevert = determineCollateralsToRevert(facility, serialsToRevert);

        if (collateralsToRevert.isEmpty()) {
            log.info(
                    "No collaterals to revert for facility: {}",
                    facility.getId().value());
            return;
        }

        if (facility.getLoanApplication().getApplicationNumber().isPresent()) {
            ApplicationNumber appNumber =
                    facility.getLoanApplication().getApplicationNumber().get();

            for (Collateral collateral : collateralsToRevert) {
                try {
                    collateralServicePort.unReserveCollateral(
                            collateral.collateralSerial(), appNumber, UUID.randomUUID(), requestId);

                    log.debug(
                            "Successfully un-reserved collateral: {}",
                            collateral.collateralSerial().value());
                } catch (Exception e) {
                    log.error(
                            "Failed to un-reserve collateral {} during full lifecycle revert",
                            collateral.collateralSerial().value(),
                            e);
                }
            }
        }

        List<String> serialsToRevertInDomain = collateralsToRevert.stream()
                .map(c -> c.collateralSerial().value())
                .toList();

        facility.revertAddCollateral(serialsToRevertInDomain, clock)
                .onSuccess(v -> {
                    repository.save(facility);
                    events.addAll(facility.domainEvents());
                    facility.clearDomainEvents();
                    log.info("Successfully reverted {} collaterals in domain", serialsToRevertInDomain.size());
                })
                .onFailure(failure -> {
                    log.error("Failed to revert collaterals in domain: {}", failure.notification());
                });
    }

    private List<Collateral> determineCollateralsToRevert(TradeLoanFacility facility, List<String> serialsToRevert) {

        if (serialsToRevert == null || serialsToRevert.isEmpty()) {
            log.debug(
                    "No specific serials provided, reverting all {} collaterals",
                    facility.getCollaterals().size());
            return new ArrayList<>(facility.getCollaterals());
        }

        List<Collateral> result = facility.getCollaterals().stream()
                .filter(collateral ->
                        serialsToRevert.contains(collateral.collateralSerial().value()))
                .toList();

        log.debug("Filtered {} collaterals to revert from {} requested serials", result.size(), serialsToRevert.size());

        return result;
    }

    private void reverseTransaction(String transactionNumber) {
        log.info("Reversing transaction: {}", transactionNumber);
        var trackedNumber = TrackedTransactionNumber.create(transactionNumber, null, TransactionStatus.POSTED, clock);
        var result = transactionPostingPort.reverseTransaction(trackedNumber);
        if (result.isFailure()) {
            log.warn(
                    "Failed to reverse transaction {}: {}",
                    transactionNumber,
                    result.err().orElseThrow().notification());
        }
    }
}
