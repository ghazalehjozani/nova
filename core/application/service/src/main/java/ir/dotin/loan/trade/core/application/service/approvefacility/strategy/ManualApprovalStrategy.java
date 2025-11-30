package ir.dotin.loan.trade.core.application.service.approvefacility.strategy;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.GracePeriod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.InstallmentCount;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.RevocationReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LifeInsuranceId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FetchSanctionDetailsPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.SanctionDetails;
import ir.dotin.loan.trade.core.application.service.approvefacility.i18n.ApproveFacilityErrorCodes;
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
    private final FetchSanctionDetailsPort fetchSanctionDetailsPort;

    @Override
    public Result<Void> validate(
            ApproveFacilityCommand command, TradeLoanFacility facility, TradeLoanArrangement arrangement) {

        if (command.sanctionSerial() == null) {
            return Result.failure(
                    Notification.ofError(ApproveFacilityErrorCodes.SANCTION_SERIAL_REQUIRED_FOR_MANUAL_APPROVAL));
        }
        boolean isAutoApproval = facility.getLoanApplication().getApplicantChannel() == DIGITAL_BANK;

        if (isAutoApproval) {
            return Result.failure(
                    Notification.ofError(ApproveFacilityErrorCodes.MANUAL_APPROVAL_NOT_ALLOWED, DIGITAL_BANK));
        }

        return Result.success();
    }

    @Override
    public Result<Void> approve(TradeLoanFacility facility, TradeLoanArrangement arrangement) {
        return fetchSanctionDetailsPort
                .fetchBySanctionSerial(facility.getId().value().toString())
                .flatMap(this::buildSanctionedLoanBuilder)
                .flatMap(builder -> domainService.approve(facility, builder, false));
    }

    private Result<TradeSanctionedLoan.Builder> buildSanctionedLoanBuilder(SanctionDetails details) {
        try {
            TradeSanctionedLoan.Builder builder = TradeSanctionedLoan.builder()
                    .id(SanctionedLoanId.generate())
                    .sanctionSerial(SanctionSerial.of(details.sanctionSerialValue(), details.sanctionType())
                            .getValue())
                    .approvedAmount(new Money(details.approvedAmount(), details.currency()))
                    .gracePeriod(GracePeriod.of(details.gracePeriod()).getValue())
                    .installmentCount(
                            InstallmentCount.of(details.installmentCount()).getValue())
                    .loanDuration(LoanDuration.of(details.loanDuration()).getValue())
                    .disbursementMethod(details.disbursementMethod());

            if (details.lifeInsuranceId() != null) {
                builder.lifeInsuranceId(
                        LifeInsuranceId.of(details.lifeInsuranceId()).getValue());
            }

            if (details.collateralSerial() != null) {
                builder.collateralSerial(
                        CollateralSerial.of(details.collateralSerial()).getValue());
            }

            if (details.revocationReason() != null) {
                builder.revocationReason(
                        RevocationReason.of(details.revocationReason()).getValue());
            }

            return Result.success(builder);
        } catch (Exception e) {
            return Result.failure(
                    Notification.ofError(ApproveFacilityErrorCodes.INVALID_SANCTION_DETAILS, e.getMessage()));
        }
    }
}
