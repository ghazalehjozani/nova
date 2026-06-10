package ir.dotin.loan.trade.core.application.service.planequalinstallmentschedule.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteCommandHandler;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteTransaction;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentScheduleCreationContext;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanEqualInstallmentScheduleCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.installmentschedule.service.TradeRepaymentSchedulingService;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

@Service
public class PlanEqualInstallmentScheduleCommandHandler
        extends WriteCommandHandler<PlanEqualInstallmentScheduleCommand, Unit> {

    private static final Logger log = LoggerFactory.getLogger(PlanEqualInstallmentScheduleCommandHandler.class);

    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final TradeLoanFacilityRepository tradeLoanFacilityRepository;
    private final TradeLoanArrangementRepository tradeLoanArrangementRepository;
    private final TradeRepaymentSchedulingService schedulingService;
    private final Clock clock;

    public PlanEqualInstallmentScheduleCommandHandler(
            WriteTransaction writeTransaction,
            InstallmentScheduleRepository installmentScheduleRepository,
            TradeLoanFacilityRepository tradeLoanFacilityRepository,
            TradeLoanArrangementRepository tradeLoanArrangementRepository,
            TradeRepaymentSchedulingService schedulingService,
            Clock clock) {
        super(writeTransaction);
        this.installmentScheduleRepository = installmentScheduleRepository;
        this.tradeLoanFacilityRepository = tradeLoanFacilityRepository;
        this.tradeLoanArrangementRepository = tradeLoanArrangementRepository;
        this.schedulingService = schedulingService;
        this.clock = clock;
    }

    @Override
    protected Result<Unit> prepare(PlanEqualInstallmentScheduleCommand command) {
        return Result.success();
    }

    @Override
    protected Result<List<DomainEvent<?>>> write(PlanEqualInstallmentScheduleCommand command, Unit prepared) {
        return loadDependencies(command)
                .flatMap(this::planSchedule)
                .onSuccess(installmentScheduleRepository::save)
                .onSuccess(installmentSchedule ->
                        log.info("Equal installment schedule created: {}", installmentSchedule.getId()))
                .map(AbstractAggregateRoot::domainEvents);
    }

    private Result<ScheduleCreationDependencies> loadDependencies(PlanEqualInstallmentScheduleCommand command) {
        Result<TradeLoanFacility> facility = Result.fromOptional(
                tradeLoanFacilityRepository.findById(LoanFacilityId.of(command.loanFacilityId())),
                FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())));

        return facility.flatMap(f -> {
            Result<TradeLoanArrangement> arrangement = Result.fromOptional(
                    tradeLoanArrangementRepository.findById(f.getLoanArrangementId()),
                    FailureCause.notFound(Notification.ofError(
                            TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND,
                            f.getLoanArrangementId().value())));

            return arrangement.map(a -> new ScheduleCreationDependencies(f, a));
        });
    }

    private Result<InstallmentSchedule> planSchedule(ScheduleCreationDependencies dependencies) {
        InstallmentScheduleCreationContext context =
                new InstallmentScheduleCreationContext(dependencies.facility(), dependencies.arrangement(), clock);
        return schedulingService.planEqualInstallmentSchedule(context);
    }

    private record ScheduleCreationDependencies(TradeLoanFacility facility, TradeLoanArrangement arrangement) {}
}
