package ir.dotin.loan.trade.core.application.service.approvefacility.strategy;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.service.approvefacility.i18n.ApproveFacilityErrorCodes;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AutoApprovalStrategy implements ApprovalStrategy {

    private final TradeLoanFacilityService domainService;

    @Override
    public Result<Void> validate(
            ApproveFacilityCommand command, TradeLoanFacility facility, TradeLoanArrangement arrangement) {

        if (command.sanctionSerial() != null) {
            return Result.failure(
                    Notification.ofError(ApproveFacilityErrorCodes.SANCTION_SERIAL_NOT_ALLOWED_FOR_AUTO_APPROVAL));
        }

        if (!arrangement.isAutoApproval()) {
            return Result.failure(Notification.ofError(
                    ApproveFacilityErrorCodes.AUTO_APPROVAL_NOT_ENABLED,
                    facility.getLoanArrangementId().value()));
        }

        return Result.success();
    }

    @Override
    public Result<Void> approve(TradeLoanFacility facility, TradeLoanArrangement arrangement) {
        return domainService.approve(facility, null, arrangement);
    }
}
