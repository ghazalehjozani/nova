package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentScheduleCreationContext;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.SchedulePlanRequest;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.ProductProfile;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;
import ir.dotin.loan.trade.core.domain.installmentschedule.service.TradeRepaymentSchedulingService;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OriginationSchedulePlanner {

    private final TradeRepaymentSchedulingService schedulingService;
    private final Clock clock;

    public Result<InstallmentSchedule> planSystemGenerated(
            TradeLoanApplication application,
            FacilityOriginationContext context,
            ProductProfile profile,
            LoanFacilityId facilityId) {

        return plan(application, context, profile, facilityId, SchedulePlanRequest.deferToProfile());
    }

    public Result<InstallmentSchedule> planFromSuppliedTable(
            TradeLoanApplication application,
            FacilityOriginationContext context,
            ProductProfile profile,
            LoanFacilityId facilityId,
            List<InstallmentSpec> suppliedTable) {

        return plan(application, context, profile, facilityId, SchedulePlanRequest.withSuppliedTable(suppliedTable));
    }

    // why: the schedule is planned against a facility that does not exist yet, so a transient one carrying the
    // eventual id is built purely to satisfy the creation context.
    private Result<InstallmentSchedule> plan(
            TradeLoanApplication application,
            FacilityOriginationContext context,
            ProductProfile profile,
            LoanFacilityId facilityId,
            SchedulePlanRequest request) {

        TradeLoanFacility transientFacility = TradeLoanFacility.create(
                facilityId,
                application,
                context.loanType().getId(),
                context.arrangement().getId(),
                clock,
                null);

        InstallmentScheduleCreationContext scheduleContext =
                new InstallmentScheduleCreationContext(transientFacility, context.arrangement(), clock);

        return schedulingService.planSchedule(scheduleContext, profile, request);
    }
}
