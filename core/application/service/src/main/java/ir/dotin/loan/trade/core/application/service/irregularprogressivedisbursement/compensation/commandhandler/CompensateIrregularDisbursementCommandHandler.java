package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.compensation.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.RevertRestructuringResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateIrregularDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.i18n.IrregularProgressiveDisbursementErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
class CompensateIrregularDisbursementCommandHandler implements CommandHandler<CompensateIrregularDisbursementCommand> {

    private final TradeLoanFacilityRepository facilityRepository;
    private final InstallmentScheduleRepository scheduleRepository;
    private final TransactionPostingPort transactionPostingPort;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(CompensateIrregularDisbursementCommand command) {
        log.warn("Compensating irregular disbursement for facility: {}", command.loanFacilityId());

        return Result.fromOptional(
                        facilityRepository.findById(LoanFacilityId.of(command.loanFacilityId())),
                        () -> Notification.ofError(
                                IrregularProgressiveDisbursementErrorCodes.FACILITY_NOT_FOUND,
                                command.loanFacilityId()))
                .flatMap(facility -> revertDisbursementAndSchedules(facility, command));
    }

    private Result<List<DomainEvent<?>>> revertDisbursementAndSchedules(
            TradeLoanFacility facility, CompensateIrregularDisbursementCommand command) {

        List<DomainEvent<?>> allEvents = new ArrayList<>();

        return facility.revertIrregularTrancheDisbursement(clock).flatMap(removedTransactionOpt -> {
            removedTransactionOpt.ifPresent(this::reverseTransaction);

            facilityRepository.save(facility);
            allEvents.addAll(facility.domainEvents());

            return handleScheduleCompensation(command.installmentScheduleId(), allEvents);
        });
    }

    private Result<List<DomainEvent<?>>> handleScheduleCompensation(
            java.util.UUID installmentScheduleId, List<DomainEvent<?>> allEvents) {

        if (installmentScheduleId == null) {
            return Result.success(allEvents);
        }

        Optional<InstallmentSchedule> currentScheduleOpt = scheduleRepository.findById(
                InstallmentScheduleId.of(installmentScheduleId).getValue());

        if (currentScheduleOpt.isEmpty()) {
            log.warn("Current InstallmentSchedule not found: {}", installmentScheduleId);
            return Result.success(allEvents);
        }

        InstallmentSchedule currentSchedule = currentScheduleOpt.get();

        return revertCurrentSchedule(currentSchedule, allEvents);
    }

    private Result<List<DomainEvent<?>>> revertCurrentSchedule(
            InstallmentSchedule currentSchedule, List<DomainEvent<?>> allEvents) {

        boolean hasHistory = !currentSchedule.getScheduleHistory().isEmpty();

        if (hasHistory) {
            return revertRestructuredSchedule(currentSchedule, allEvents);
        } else {
            return revertNewSchedule(currentSchedule, allEvents);
        }
    }

    private Result<List<DomainEvent<?>>> revertRestructuredSchedule(
            InstallmentSchedule currentSchedule, List<DomainEvent<?>> allEvents) {

        return currentSchedule
                .revertRestructuring(clock)
                .flatMap(result -> {
                    scheduleRepository.save(currentSchedule);
                    allEvents.addAll(currentSchedule.domainEvents());
                    log.info(
                            "Reverted restructured schedule: {}",
                            currentSchedule.getId().value());

                    return reactivatePreviousScheduleIfPresent(result, allEvents);
                })
                .recover(notification -> {
                    log.warn("Failed to revert restructured schedule: {}. Trying new schedule revert.", notification);
                    return revertNewSchedule(currentSchedule, allEvents);
                });
    }

    private Result<List<DomainEvent<?>>> reactivatePreviousScheduleIfPresent(
            RevertRestructuringResult result, List<DomainEvent<?>> allEvents) {

        return result.previousScheduleId()
                .flatMap(scheduleRepository::findById)
                .map(previousSchedule -> reactivatePreviousSchedule(previousSchedule, allEvents))
                .orElseGet(() -> {
                    result.previousScheduleId()
                            .ifPresent(id -> log.warn("Previous schedule not found for reactivation: {}", id.value()));
                    return Result.success(allEvents);
                });
    }

    private Result<List<DomainEvent<?>>> reactivatePreviousSchedule(
            InstallmentSchedule previousSchedule, List<DomainEvent<?>> allEvents) {

        return previousSchedule
                .revertSuperseded(clock)
                .flatMap(ignored -> {
                    scheduleRepository.save(previousSchedule);
                    allEvents.addAll(previousSchedule.domainEvents());
                    log.info(
                            "Reactivated previous schedule: {}",
                            previousSchedule.getId().value());
                    return Result.success(allEvents);
                })
                .recover(notification -> {
                    log.warn("Failed to reactivate previous schedule: {}", notification);
                    return Result.success(allEvents);
                });
    }

    private Result<List<DomainEvent<?>>> revertNewSchedule(
            InstallmentSchedule currentSchedule, List<DomainEvent<?>> allEvents) {

        return currentSchedule
                .revertActivation(clock)
                .flatMap(ignored -> currentSchedule.revertInstallmentCreation(clock))
                .flatMap(removedIds -> {
                    log.info(
                            "Reverted {} installments from schedule: {}",
                            removedIds.size(),
                            currentSchedule.getId().value());
                    scheduleRepository.save(currentSchedule);
                    allEvents.addAll(currentSchedule.domainEvents());
                    return Result.success(allEvents);
                })
                .recover(notification -> {
                    log.warn("Failed to revert new schedule: {}", notification);
                    return Result.success(allEvents);
                });
    }

    private void reverseTransaction(TrackedTransactionNumber trackedNumber) {
        log.info("Reversing irregular disbursement transaction: {}", trackedNumber.value());
        var reverseResult = transactionPostingPort.reverseTransaction(trackedNumber);
        if (reverseResult.hasErrors()) {
            log.error(
                    "Failed to reverse irregular disbursement transaction: {}. Errors: {}",
                    trackedNumber.value(),
                    reverseResult.notification());
        }
    }
}
