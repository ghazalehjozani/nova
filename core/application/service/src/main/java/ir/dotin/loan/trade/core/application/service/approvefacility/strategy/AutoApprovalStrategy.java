package ir.dotin.loan.trade.core.application.service.approvefacility.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
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
    public Result<Unit> validate(
            ApproveFacilityCommand command, TradeLoanFacility facility, TradeLoanArrangement arrangement) {

        if (command.sanctionSerial() != null) {
            return Result.failure(TradeLoanApplicationServiceErrors.SANCTION_SERIAL_NOT_ALLOWED_FOR_AUTO_APPROVAL);
        }
        boolean isAutoApproval = facility.getLoanApplication().getApplicantChannel() == DIGITAL_BANK;

        ConfirmType requestConfirmType = ConfirmType.of(command.confirmType()).unwrap();
        List<ConfirmType> allowedConfirmTypes = arrangement.getConfirmTypes();

        if (allowedConfirmTypes == null || !allowedConfirmTypes.contains(requestConfirmType)) {
            return Result.failure(Notification.ofError(
                    TradeLoanApplicationServiceErrors.CONFIRM_TYPE_NOT_ALLOWED,
                    command.confirmType(),
                    arrangement.getCode().value()));
        }

        if (!isAutoApproval) {
            return Result.failure(Notification.ofError(
                    TradeLoanApplicationServiceErrors.AUTO_APPROVAL_NOT_ENABLED,
                    facility.getLoanApplication().getApplicantChannel().name()));
        }

        return Result.success();
    }

    @Override
    public Result<Unit> approve(
            ApproveFacilityCommand command,
            TradeLoanFacility facility,
            TradeLoanArrangement arrangement,
            ConfirmType confirmType) {
        // Auto approval makes no FCB call and needs no pre-flight data — the command argument is intentionally unused.
        return domainService.approve(facility, null, true, confirmType);
    }
}
