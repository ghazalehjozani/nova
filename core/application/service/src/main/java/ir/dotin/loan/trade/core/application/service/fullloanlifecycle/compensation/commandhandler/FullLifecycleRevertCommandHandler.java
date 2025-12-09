package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.compensation.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.TransactionStatus;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLifecycleRevertCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.fullloanlifecycle.i18n.FullLoanFacilityLifecycleErrorCodes;
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
                Notification.ofError(FullLoanFacilityLifecycleErrorCodes.FACILITY_NOT_FOUND, facilityId));
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
                        FullLoanFacilityLifecycleErrorCodes.INVALID_STATE_FOR_COMPENSATION,
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
                .peekValue(v -> {
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
                .peekValue(v -> {
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
                .peekValue(v -> {
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
            revertCollaterals(facility, command.uid(), events);
        }

        return facility.revertApproval(clock)
                .peekValue(v -> {
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
                .peekValue(v -> {
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
                .peekValue(v -> {
                    repository.save(facility);
                    events.addAll(facility.domainEvents());
                })
                .map(v -> events);
    }

    private void revertCollaterals(TradeLoanFacility facility, UUID requestId, List<DomainEvent<?>> events) {

        log.info(
                "Reverting {} collaterals for facility: {}",
                facility.getCollaterals().size(),
                facility.getId().value());

        if (facility.getLoanApplication().getApplicationNumber().isPresent()) {
            ApplicationNumber appNumber =
                    facility.getLoanApplication().getApplicationNumber().get();
            for (Collateral collateral : facility.getCollaterals()) {
                collateralServicePort.unReserveCollateral(
                        collateral.collateralSerial(), appNumber, requestId, UUID.randomUUID());
            }
        }

        facility.revertAddCollateral(clock).peekValue(v -> {
            repository.save(facility);
            events.addAll(facility.domainEvents());
            facility.clearDomainEvents();
        });
    }

    private void reverseTransaction(String transactionNumber) {
        log.info("Reversing transaction: {}", transactionNumber);
        var trackedNumber = TrackedTransactionNumber.create(transactionNumber, null, TransactionStatus.POSTED, clock);
        var result = transactionPostingPort.reverseTransaction(trackedNumber);
        if (result.hasErrors()) {
            log.warn("Failed to reverse transaction {}: {}", transactionNumber, result.notification());
        }
    }
}
