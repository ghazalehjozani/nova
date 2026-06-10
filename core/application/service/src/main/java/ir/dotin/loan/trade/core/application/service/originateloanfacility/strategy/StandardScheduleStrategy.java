package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import java.util.Optional;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StandardScheduleStrategy implements InstallmentScheduleStrategy {

    @Override
    public Result<Optional<InstallmentSchedule>> planSchedule(
            OriginateLoanFacilityCommand command,
            TradeLoanApplication application,
            FacilityOriginationContext context,
            LoanFacilityId facilityId) {

        log.debug(
                "No installment schedule planning required for {} payment type",
                context.arrangement().getInstallmentPolicy().installmentPaymentType());

        return Result.success(Optional.empty());
    }

    @Override
    public Result<Unit> validateCommand(OriginateLoanFacilityCommand command) {
        Notification notification = Notification.create();
        if (command.loanApplication().installmentCount() == null
                || command.loanApplication().installmentCount().value() == null) {
            notification.addError(OriginateLoanFacilityErrorCodes.INSTALLMENT_COUNT_CANNOT_BE_EMPTY);
        }
        return notification.isEmpty() ? Result.success() : Result.failure(notification);
    }
}
