package ir.dotin.loan.trade.core.application.service.restructuringfacility.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.service.InstallmentRecalculationService;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LoanFacilityRestructuringCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.service.restructuringfacility.mapper.LoanFacilityRestructuringInstallmentSchedulePlanMapper;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class LoanFacilityRestructuringCommandHandler implements CommandHandler<LoanFacilityRestructuringCommand> {

    private static final Logger log = LoggerFactory.getLogger(LoanFacilityRestructuringCommandHandler.class);

    private final TradeLoanFacilityRepository tradeLoanFacilityRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final ApplicationNumberResolver applicationNumberResolver;
    private final LoanFacilityRestructuringInstallmentSchedulePlanMapper schedulePlanMapper;
    private final InstallmentRecalculationService recalculationService;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(LoanFacilityRestructuringCommand command) {
        return resolveIdentifier(command).flatMap(this::loadFacility).flatMap(facility -> loadDependencies(
                        facility, command)
                .flatMap(context -> validateAll(facility, context)
                        .flatMap(ignored -> processRestructuring(context))
                        .flatMap(this::persistAndCollectEvents)
                        .onSuccess(schedule -> log.info(
                                "Loan facility restructuring completed: applicationNumber={}, ref={}",
                                command.applicationNumber(),
                                command.transactionReference()))
                        .map(RestructuringResult::events)));
    }

    private Result<RestructuringResult> persistAndCollectEvents(RestructuringOperationResult operationResult) {
        installmentScheduleRepository.save(operationResult.oldSchedule());
        InstallmentSchedule newSchedule = installmentScheduleRepository.save(operationResult.newSchedule());
        TradeLoanFacility savedFacility = tradeLoanFacilityRepository.save(operationResult.facility());

        List<DomainEvent<?>> events = new ArrayList<>();
        events.addAll(operationResult.oldSchedule().domainEvents());
        events.addAll(operationResult.newSchedule().domainEvents());
        events.addAll(operationResult.facility().domainEvents());

        return Result.success(new RestructuringResult(savedFacility, newSchedule, events));
    }

    private Result<LoanFacilityId> resolveIdentifier(LoanFacilityRestructuringCommand command) {
        return Result.fromOptional(
                applicationNumberResolver.resolveLoanFacilityIdByApplicationNumber(command.applicationNumber()),
                () -> FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING, command.applicationNumber())));
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                tradeLoanFacilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }

    private Result<ProcessingContext> loadDependencies(
            TradeLoanFacility facility, LoanFacilityRestructuringCommand command) {
        List<InstallmentSpec> customPlan = null;
        CurrencyType currencyType = facility.getSanctionedLoan().orElseThrow().getCurrency();
        if (command.installmentSchedulePlan() != null) {
            customPlan = schedulePlanMapper.mapSpecs(
                    command.installmentSchedulePlan().installments(), currencyType);
        }
        List<InstallmentSpec> finalCustomPlan = customPlan;
        return loadInstallmentSchedule(facility)
                .map(schedule -> new ProcessingContext(
                        command.transactionReference(),
                        facility,
                        schedule,
                        finalCustomPlan,
                        command.userId(),
                        command.duration()));
    }

    private Result<InstallmentSchedule> loadInstallmentSchedule(TradeLoanFacility facility) {
        return facility.getInstallmentScheduleId()
                .map(scheduleId -> Result.fromOptional(
                        installmentScheduleRepository.findById(scheduleId),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                                facility.getId().value()))))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                        facility.getId().value())));
    }

    private Result<Unit> validateAll(TradeLoanFacility facility, ProcessingContext context) {
        return Result.success();
    }

    private Result<RestructuringOperationResult> processRestructuring(ProcessingContext context) {
        return recalculateSchedule(context).flatMap(recalculatedInstallments -> restructureAndActivateSchedule(
                        context.facility(), context, recalculatedInstallments)
                .flatMap(newSchedule -> performRestructuringOperations(context.facility(), context, newSchedule)));
    }

    private Result<RestructuringOperationResult> performRestructuringOperations(
            TradeLoanFacility facility, ProcessingContext context, InstallmentSchedule newSchedule) {

        return facility.restructuringFacility(
                        context.duration(),
                        context.restructuringTransaction(),
                        newSchedule.getId(),
                        clock,
                        context.userId())
                .map(ignored -> new RestructuringOperationResult(facility, context.schedule(), newSchedule));
    }

    private Result<List<Installment>> recalculateSchedule(ProcessingContext context) {
        return recalculationService.recalculateForFacilityRestructuring(
                context.schedule(), context.facility(), context.customPlan());
    }

    private Result<InstallmentSchedule> restructureAndActivateSchedule(
            TradeLoanFacility facility, ProcessingContext context, List<Installment> recalculatedInstallments) {
        String reason = String.format("restructuring transaction: %s", context.restructuringTransaction);
        return context.schedule()
                .restructureSchedule(
                        recalculatedInstallments,
                        reason,
                        facility.getTotalDisbursedAmount(),
                        requireNonNull(
                                facility.getSanctionedLoan().orElseThrow().getApprovedAmount()),
                        clock,
                        context.userId())
                .flatMap(newSchedule -> newSchedule.activateSchedule(clock).map(ignored -> newSchedule));
    }

    private record ProcessingContext(
            String restructuringTransaction,
            TradeLoanFacility facility,
            InstallmentSchedule schedule,
            @Nullable List<InstallmentSpec> customPlan,
            String userId,
            Integer duration) {}

    private record RestructuringOperationResult(
            TradeLoanFacility facility, InstallmentSchedule oldSchedule, InstallmentSchedule newSchedule) {}

    private record RestructuringResult(
            TradeLoanFacility facility, InstallmentSchedule schedule, List<DomainEvent<?>> events) {}
}
