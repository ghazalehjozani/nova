package ir.dotin.loan.trade.core.application.service.approvefacility.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.service.approvefacility.i18n.ApproveFacilityErrorCodes;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;

import static ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel.DIGITAL_BANK;

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
        boolean isAutoApproval = facility.getLoanApplication().getApplicantChannel() == DIGITAL_BANK;

        ConfirmType requestConfirmType = ConfirmType.of(command.confirmType()).value();
        List<ConfirmType> allowedConfirmTypes = arrangement.getConfirmTypes();

        if (allowedConfirmTypes == null || !allowedConfirmTypes.contains(requestConfirmType)) {
            return Result.failure(Notification.ofError(
                    ApproveFacilityErrorCodes.CONFIRM_TYPE_NOT_ALLOWED,
                    command.confirmType(),
                    arrangement.getCode().value()));
        }

        if (!isAutoApproval) {
            return Result.failure(Notification.ofError(
                    ApproveFacilityErrorCodes.AUTO_APPROVAL_NOT_ENABLED,
                    facility.getLoanApplication().getApplicantChannel().name()));
        }

        return Result.success();
    }

    @Override
    public Result<Void> approve(TradeLoanFacility facility, TradeLoanArrangement arrangement, ConfirmType confirmType) {
        return domainService.approve(facility, null, true, confirmType);
    }
}
