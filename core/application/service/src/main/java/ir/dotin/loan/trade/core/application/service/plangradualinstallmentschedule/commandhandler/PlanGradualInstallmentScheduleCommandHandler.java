package ir.dotin.loan.trade.core.application.service.plangradualinstallmentschedule.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentScheduleCreationContext;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanGradualInstallmentScheduleCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.plangradualinstallmentschedule.i18n.PlanUnequalInstallmentScheduleErrorCodes;
import ir.dotin.loan.trade.core.application.service.plangradualinstallmentschedule.mapper.PlanGradualInstallmentScheduleCommandMapper;
import ir.dotin.loan.trade.core.domain.installmentschedule.service.TradeRepaymentSchedulingService;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanGradualInstallmentScheduleCommandHandler
        implements CommandHandler<PlanGradualInstallmentScheduleCommand> {

    private static final Logger log = LoggerFactory.getLogger(PlanGradualInstallmentScheduleCommandHandler.class);

    private final PlanGradualInstallmentScheduleCommandMapper mapper;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final TradeLoanFacilityRepository tradeLoanFacilityRepository;
    private final TradeLoanArrangementRepository tradeLoanArrangementRepository;
    private final TradeRepaymentSchedulingService schedulingService;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(PlanGradualInstallmentScheduleCommand command) {
        return loadDependencies(command)
                .flatMap(context -> planSchedule(command, context))
                .peekValue(installmentScheduleRepository::save)
                .peekValue(installmentSchedule ->
                        log.debug("Unequal installment schedule created: {}", installmentSchedule.getId()))
                .mapNonNull(AbstractAggregateRoot::domainEvents);
    }

    private Result<ScheduleCreationDependencies> loadDependencies(PlanGradualInstallmentScheduleCommand command) {
        Result<TradeLoanFacility> facility = Result.fromOptional(
                tradeLoanFacilityRepository.findById(LoanFacilityId.of(command.loanFacilityId())),
                Notification.ofError(
                        PlanUnequalInstallmentScheduleErrorCodes.FACILITY_NOT_FOUND, command.loanFacilityId()));

        return facility.flatMap(f -> {
            Result<TradeLoanArrangement> arrangement = Result.fromOptional(
                    tradeLoanArrangementRepository.findById(f.getLoanArrangementId()),
                    Notification.ofError(
                            PlanUnequalInstallmentScheduleErrorCodes.ARRANGEMENT_NOT_FOUND,
                            f.getLoanArrangementId().value()));

            return arrangement.mapNonNull(a -> new ScheduleCreationDependencies(f, a));
        });
    }

    private Result<InstallmentSchedule> planSchedule(
            PlanGradualInstallmentScheduleCommand command, ScheduleCreationDependencies dependencies) {

        InstallmentScheduleCreationContext<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> context =
                new InstallmentScheduleCreationContext<>(dependencies.facility(), dependencies.arrangement(), clock);

        List<InstallmentSpec> installmentSpecs = mapper.mapSpec(command.installments());

        return schedulingService.planGradualInstallmentSchedule(context, installmentSpecs);
    }

    private record ScheduleCreationDependencies(TradeLoanFacility facility, TradeLoanArrangement arrangement) {}
}
