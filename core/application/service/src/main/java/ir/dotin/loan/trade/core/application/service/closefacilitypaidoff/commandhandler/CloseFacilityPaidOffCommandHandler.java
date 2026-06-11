package ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.commandhandler;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.CloseInstallmentSchedulePaidOff;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CloseFacilityPaidOffInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityPaidOffCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

@Service
public final class CloseFacilityPaidOffCommandHandler extends WorkflowCommandHandler<CloseFacilityPaidOffCommand, CloseFacilityPaidOffCommandHandler.Data> {

    private static final Logger log = LoggerFactory.getLogger(CloseFacilityPaidOffCommandHandler.class);

    record Data(CloseFacilityPaidOffCommand command, Unit prepared) {}

    private final TradeLoanFacilityService domainService;
    private final TradeLoanFacilityRepository repository;

    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final ApplicationNumberResolver applicationNumberResolver;
    private final Clock clock;
    private final Workflow<Data> workflow;

    public CloseFacilityPaidOffCommandHandler(
            WorkflowEngine engine,
            TradeLoanFacilityService domainService,
            TradeLoanFacilityRepository repository,
            InstallmentScheduleRepository installmentScheduleRepository,
            ApplicationNumberResolver applicationNumberResolver,
            Clock clock) {
        super(engine);
        this.domainService = domainService;
        this.repository = repository;
        this.installmentScheduleRepository = installmentScheduleRepository;
        this.applicationNumberResolver = applicationNumberResolver;
        this.clock = clock;
        this.workflow = Workflow.singleWrite(
                "close-facility-paid-off",
                ctx -> StepResult.fromWriteResult(write(ctx.data().command(), ctx.data().prepared())));
    }

    @Override
    protected Workflow<Data> workflow() {
        return workflow;
    }

    @Override
    protected Result<Data> seed(CloseFacilityPaidOffCommand command) {
        return prepare(command).map(prepared -> new Data(command, prepared));
    }

    private Result<Unit> prepare(CloseFacilityPaidOffCommand command) {
        return Result.success();
    }

    private Result<List<DomainEvent<?>>> write(CloseFacilityPaidOffCommand command, Unit prepared) {
        List<DomainEvent<?>> allEvents = new ArrayList<>();
        return resolveIdentifiers(command)
                .flatMap(ids -> loadSchedule(ids, command)
                        .flatMap(schedule -> applyClosePaidOffPayments(schedule, command))
                        .onSuccess(schedule -> {
                            allEvents.addAll(schedule.domainEvents());
                        })
                        .onSuccess(installmentScheduleRepository::save)
                        .onSuccess(schedule -> log.info(
                                "Installment collection completed: applicationNumber={}, payments={}, ref={}",
                                command.applicationNumber(),
                                command.payments().size(),
                                command.transactionReference()))
                        .flatMap(__ -> loadFacility(ids, command))
                        .flatMap(facility -> closeFacility(facility, command))
                        .onSuccess(facility -> {
                            allEvents.addAll(facility.domainEvents());
                        })
                        .onSuccess(facility -> {
                            repository.save(facility);
                            log.debug("Facility closed as paid off: {}", command.applicationNumber());
                        }))
                .map(__ -> allEvents);
    }

    private Result<TradeLoanFacility> loadFacility(
            ApplicationNumberResolver.LoanIdentifiers ids, CloseFacilityPaidOffCommand command) {
        return Result.fromOptional(
                repository.findById(LoanFacilityId.of(ids.loanFacilityId())),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, ids.loanFacilityId())));
    }

    private Result<ApplicationNumberResolver.LoanIdentifiers> resolveIdentifiers(CloseFacilityPaidOffCommand command) {
        return Result.fromOptional(
                applicationNumberResolver.resolveByApplicationNumber(command.applicationNumber()),
                () -> FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING, command.applicationNumber())));
    }

    private Result<InstallmentSchedule> loadSchedule(
            ApplicationNumberResolver.LoanIdentifiers ids, CloseFacilityPaidOffCommand command) {
        return Result.fromOptional(
                installmentScheduleRepository.findById(
                        InstallmentScheduleId.of(ids.installmentScheduleId()).unwrap()),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                        ids.installmentScheduleId())));
    }

    private Result<TradeLoanFacility> closeFacility(TradeLoanFacility facility, CloseFacilityPaidOffCommand command) {
        return CloseFacilityPaidOffInfo.of(
                        LocalDate.now(clock),
                        calculateTotalClosePaidOffAmount(
                                command, facility.getLoanApplication().getCurrency()))
                .flatMap(info -> domainService.closePaidOff(info, command.transactionReference(), facility))
                .map(v -> facility);
    }

    private Result<InstallmentSchedule> applyClosePaidOffPayments(
            InstallmentSchedule schedule, CloseFacilityPaidOffCommand command) {

        CurrencyType currency = schedule.getCurrency();
        List<CloseInstallmentSchedulePaidOff.CloseInstallmentSchedulePaidOffItem> closeInstallmentSchedulePaidOffItems =
                new ArrayList<>();

        for (CloseFacilityPaidOffCommand.InstallmentPaymentItem item : command.payments()) {
            Result<CloseInstallmentSchedulePaidOff.CloseInstallmentSchedulePaidOffItem> itemResult =
                    buildCloseInstallmentSchedulePaidOffItem(item, command, currency);

            if (itemResult.isFailure()) {
                return Result.failure(itemResult.err().orElseThrow());
            }

            closeInstallmentSchedulePaidOffItems.add(itemResult.unwrap());
        }

        CloseInstallmentSchedulePaidOff closePaidOff = new CloseInstallmentSchedulePaidOff(
                command.transactionReference(),
                closeInstallmentSchedulePaidOffItems,
                Objects.requireNonNullElse(command.channel(), ""));

        return schedule.closePaidOff(closePaidOff, clock).map(ignored -> schedule);
    }

    private Result<CloseInstallmentSchedulePaidOff.CloseInstallmentSchedulePaidOffItem>
            buildCloseInstallmentSchedulePaidOffItem(
                    CloseFacilityPaidOffCommand.InstallmentPaymentItem item,
                    CloseFacilityPaidOffCommand command,
                    CurrencyType currency) {

        Result<Money> principalResult = Money.valueOf(item.principalAmount(), currency);

        Result<Money> interestResult = Money.valueOf(item.interestAmount(), currency);

        Result<Money> totalResult = Money.valueOf(item.totalPaidAmount(), currency);

        Result<Money> penaltyResult = Money.zero(currency);
        if (penaltyResult.isFailure()) {
            return Result.failure(penaltyResult.err().orElseThrow());
        }

        LocalDate valueDate = item.valueDate() != null ? item.valueDate() : item.paymentDate();

        CloseInstallmentSchedulePaidOff.CloseInstallmentSchedulePaidOffItem closeInstallmentSchedulePaidOffItem =
                new CloseInstallmentSchedulePaidOff.CloseInstallmentSchedulePaidOffItem(
                        item.installmentSequenceNumber(),
                        principalResult.unwrap(),
                        interestResult.unwrap(),
                        penaltyResult.unwrap(),
                        totalResult.unwrap(),
                        valueDate);

        return Result.success(closeInstallmentSchedulePaidOffItem);
    }

    private Money calculateTotalClosePaidOffAmount(CloseFacilityPaidOffCommand command, CurrencyType currency) {
        return command.payments().stream()
                .map(item -> Money.valueOf(item.totalPaidAmount(), currency).unwrap())
                .reduce(Money.zero(currency).unwrap(), (a, b) -> a.add(b).unwrap());
    }
}
