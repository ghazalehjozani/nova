package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.math.BigDecimal;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.CollateralCalculationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PenaltyPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.*;

import lombok.Builder;

@Builder(toBuilder = true)
public record DefineTradeLoanArrangementCommand(
        @NotNull UUID uid,
        @Nullable Long version,
        @NotNull @Valid LoanArrangementCodeDto code,
        @NotNull @Valid TitleDto title,
        @NotNull @Valid CurrencyTypeDto currencyType,
        @NotNull @Valid AmountRangeDto amountRange,
        @NotNull @Valid LoanDurationRangeDto durationRange,
        @NotNull @Valid PartyType partyType,
        @NotNull @Valid List<@NotNull ConfirmTypeDto> confirmTypes,
        @NotNull Integer guarantorCount,
        @NotNull Boolean hasInstallmentCard,
        @NotNull @Valid LifeInsurancePaymentType lifeInsurancePaymentType,
        @NotNull @Valid LoanSecondaryType loanSecondaryType,
        @NotNull @Valid SectionType sectionType,
        @NotNull @Valid EconomicSectorDto economicSector,
        @NotNull @Valid InterestPolicyDto interestPolicy,
        @NotNull @Valid PenaltyPolicyDto penaltyPolicy,
        @NotNull @Valid InstallmentPolicyDto installmentPolicy,
        @NotNull @Valid GracePeriodPolicyDto gracePeriodPolicy,
        @NotNull @Valid RepaymentPriorityPolicyDto repaymentPriorityPolicy,
        @NotNull @Valid RegulatoryCompliancePolicyDto regulatoryCompliancePolicy,
        @NotNull @Valid CollateralPolicyDto collateralPolicy,
        @NotNull @Valid DisbursementType disbursementType)
        implements Command {

    public record InterestPolicyDto(
            @NotNull @DecimalMin(value = "0") BigDecimal rate,
            @NotNull @DecimalMax(value = "0") BigDecimal minPreferentialRate,
            @NotNull @DecimalMin(value = "0") BigDecimal maxPreferentialRate,
            @NotBlank String interestFormula,
            @NotBlank String refundFormula,
            @NotNull Boolean dailyInterest) {}

    public record PenaltyPolicyDto(
            @NotNull @DecimalMin(value = "0") BigDecimal penaltyRate,
            @NotNull @DecimalMin(value = "0") BigDecimal deferralInterestRate,
            @NotBlank String formula,
            @NotNull PenaltyPaymentType paymentType) {}

    public record InstallmentPolicyDto(
            @NotNull Period installmentPeriod,
            @NotBlank String installmentFormula,
            @NotBlank String interestComponentFormula,
            @NotNull InstallmentPaymentType paymentType) {}

    public record GracePeriodPolicyDto(
            @NotNull @Min(0) Integer minGracePeriodDays,
            @NotNull @Min(0) Integer maxGracePeriodDays,
            @NotBlank String formula) {}

    public record RepaymentPriorityPolicyDto(
            @NotNull Integer principalPriority,
            @NotNull Integer interestPriority,
            @NotNull Integer penaltyPriority,
            @NotNull Integer commissionPriority,
            @NotNull Integer insurancePriority,
            @NotNull Integer insurancePenaltyPriority,
            @NotNull Boolean hasEqualPriority) {}

    public record RegulatoryCompliancePolicyDto(
            @NotNull @Min(0) Integer overDuePeriod,
            @NotNull @Min(0) Integer deferralPeriod,
            @NotNull @Min(0) Integer suspiciousPeriod) {}

    public record CollateralPolicyDto(
            @NotNull @Min(0) Integer totalPercent,
            @NotNull @Valid Set<CollateralTypeDto> collateralTypes,
            @NotNull CollateralCalculationType collateralCalculationType) {}

    public record CollateralTypeDto(@NotNull @Valid CollateralType type) {}

    public record AmountRangeDto(
            @NotNull @Valid AmountDto min, @NotNull @Valid AmountDto max) {}

    public record LoanDurationRangeDto(
            @NotNull Period min, @NotNull Period max) {}

    public record ConfirmTypeDto(@NotBlank String personCode) {}
}
