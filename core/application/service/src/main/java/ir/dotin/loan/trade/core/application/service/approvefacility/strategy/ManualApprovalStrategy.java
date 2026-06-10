package ir.dotin.loan.trade.core.application.service.approvefacility.strategy;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.GracePeriod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.InstallmentCount;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.RevocationReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LifeInsuranceId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.SanctionDetails;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;

import static ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel.DIGITAL_BANK;

@Component
@RequiredArgsConstructor
public class ManualApprovalStrategy implements ApprovalStrategy {

    private final TradeLoanFacilityService domainService;

    @Override
    public Result<Unit> validate(
            ApproveFacilityCommand command, TradeLoanFacility facility, TradeLoanArrangement arrangement) {

        if (command.sanctionSerial() == null) {
            return Result.failure(TradeLoanApplicationServiceErrors.SANCTION_SERIAL_REQUIRED_FOR_MANUAL_APPROVAL);
        }
        boolean isAutoApproval = facility.getLoanApplication().getApplicantChannel() == DIGITAL_BANK;

        if (isAutoApproval) {
            return Result.failure(
                    Notification.ofError(TradeLoanApplicationServiceErrors.MANUAL_APPROVAL_NOT_ALLOWED, DIGITAL_BANK));
        }

        return Result.success();
    }

    @Override
    public Result<Unit> approve(
            ApproveFacilityCommand command,
            TradeLoanFacility facility,
            TradeLoanArrangement arrangement,
            ConfirmType confirmType,
            @Nullable SanctionDetails sanctionDetails) {
        SanctionDetails details =
                Objects.requireNonNull(sanctionDetails, "sanctionDetails required for manual approval");
        return buildSanctionedLoanBuilder(details)
                // null confirmType is the designed API contract for manual approval;
                // AbstractLoanFacilityService.approve ignores confirmType when isAutoApproval=false.
                .flatMap(builder -> approveManual(facility, builder));
    }

    private Result<Unit> approveManual(TradeLoanFacility facility, TradeSanctionedLoan.Builder builder) {
        return domainService.approve(facility, builder, false, null);
    }

    private Result<TradeSanctionedLoan.Builder> buildSanctionedLoanBuilder(SanctionDetails details) {
        try {
            TradeSanctionedLoan.Builder builder = TradeSanctionedLoan.builder()
                    .id(SanctionedLoanId.generate())
                    .sanctionSerial(SanctionSerial.of(details.sanctionSerialValue(), details.sanctionType())
                            .unwrap())
                    .approvedAmount(new Money(details.approvedAmount(), details.currency()))
                    .gracePeriod(GracePeriod.of(details.gracePeriod()).unwrap())
                    .installmentCount(
                            InstallmentCount.of(details.installmentCount()).unwrap())
                    .loanDuration(LoanDuration.of(details.loanDuration()).unwrap())
                    .disbursementMethod(details.disbursementMethod())
                    .confirmType(details.confirmType());

            if (details.lifeInsuranceId() != null) {
                builder.lifeInsuranceId(
                        LifeInsuranceId.of(details.lifeInsuranceId()).unwrap());
            }

            if (details.revocationReason() != null) {
                builder.revocationReason(
                        RevocationReason.of(details.revocationReason()).unwrap());
            }

            return Result.success(builder);
        } catch (Exception e) {
            return Result.failure(Notification.ofError(
                    TradeLoanApplicationServiceErrors.INVALID_SANCTION_DETAILS,
                    Objects.requireNonNullElse(e.getMessage(), e.getClass().getName())));
        }
    }
}
