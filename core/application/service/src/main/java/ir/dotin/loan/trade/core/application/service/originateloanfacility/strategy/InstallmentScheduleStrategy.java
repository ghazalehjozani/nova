package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;

public interface InstallmentScheduleStrategy {

    @NonNull
    Result<Optional<InstallmentSchedule>> planSchedule(
            @NonNull OriginateLoanFacilityCommand command,
            @NonNull TradeLoanApplication application,
            @NonNull FacilityOriginationContext context,
            @NonNull LoanFacilityId facilityId);

    @NonNull
    Result<Unit> validateCommand(@NonNull OriginateLoanFacilityCommand command);
}
