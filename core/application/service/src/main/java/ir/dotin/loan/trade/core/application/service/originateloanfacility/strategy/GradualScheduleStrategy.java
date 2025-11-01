package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentScheduleCreationContext;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.mapper.InstallmentSchedulePlanMapper;
import ir.dotin.loan.trade.core.domain.installmentschedule.service.TradeRepaymentSchedulingService;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GradualScheduleStrategy implements InstallmentScheduleStrategy {

    private final TradeRepaymentSchedulingService schedulingService;
    private final InstallmentSchedulePlanMapper installmentSchedulePlanMapper;
    private final Clock clock;

    @Override
    @NonNull
    public Result<Optional<InstallmentSchedule>> planSchedule(
            @NonNull OriginateLoanFacilityCommand command,
            @NonNull TradeLoanApplication application,
            @NonNull FacilityOriginationContext context) {

        if (command.installmentSchedulePlan() == null) {
            log.error("Installment schedule plan is mandatory for GRADUAL payment type");
            return Result.failure(
                    Notification.ofError(OriginateLoanFacilityErrorCodes.INSTALLMENT_SCHEDULE_IS_MANDATORY_IN_GRADUAL));
        }

        return planInstallmentSchedule(command.installmentSchedulePlan(), application, context)
                .map(Optional::of);
    }

    @Override
    @NonNull
    public Result<Void> validateCommand(@NonNull OriginateLoanFacilityCommand command) {
        return Result.requireTrue(
                command.installmentSchedulePlan() != null,
                Notification.ofError(OriginateLoanFacilityErrorCodes.INSTALLMENT_SCHEDULE_IS_MANDATORY_IN_GRADUAL));
    }

    private Result<InstallmentSchedule> planInstallmentSchedule(
            OriginateLoanFacilityCommand.InstallmentSchedulePlanDto planDto,
            TradeLoanApplication application,
            FacilityOriginationContext context) {

        // Create temporary facility for schedule planning
        TradeLoanFacility tempFacility = TradeLoanFacility.create(
                LoanFacilityId.generate(),
                application,
                context.loanType().getId(),
                context.arrangement().getId(),
                clock,
                null);

        InstallmentScheduleCreationContext<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> scheduleContext =
                new InstallmentScheduleCreationContext<>(tempFacility, context.arrangement(), clock);

        List<InstallmentSpec> installmentSpecs =
                installmentSchedulePlanMapper.mapSpecs(planDto.installments(), application.getCurrency());

        return schedulingService
                .planGradualInstallmentSchedule(scheduleContext, installmentSpecs)
                .peekValue(schedule -> log.debug(
                        "Installment schedule planned for GRADUAL: {}",
                        schedule.getId().value()));
    }
}
