package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.math.BigDecimal;
import java.time.Period;
import java.util.Set;
import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PenaltyPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;

import lombok.Builder;

@Builder
public record DefineTradeLoanArrangementCommand(
        @NotNull UUID uid,
        @Nullable Long version,
        @NotNull LoanArrangementCodeDto code,
        @NotNull TitleDto title,
        @NotNull CurrencyTypeDto currencyType,
        @NotNull AmountRangeDto amountRange,
        @NotNull LoanDurationRangeDto durationRange,
        @NotNull PartyType partyType,
        @NotNull ConfirmTypeDto confirmType,
        @NotNull Integer guarantorCount,
        @NotNull DisbursementMethod disbursementMethod,
        @NotNull Boolean hasInstallmentCard,
        @NotNull LifeInsurancePaymentType lifeInsurancePaymentType,
        @NotNull LoanSecondaryType loanSecondaryType,
        @NotNull SectionType sectionType,
        @NotNull Boolean autoApproval,
        @NotNull EconomicSectorDto economicSector,
        @NotNull InterestPolicyDto interestPolicy,
        @NotNull PenaltyPolicyDto penaltyPolicy,
        @NotNull InstallmentPolicyDto installmentPolicy,
        @NotNull GracePeriodPolicyDto gracePeriodPolicy,
        @NotNull RepaymentPriorityPolicyDto repaymentPriorityPolicy,
        @NotNull RegulatoryCompliancePolicyDto regulatoryCompliancePolicy,
        @NotNull CollateralPolicyDto collateralPolicy)
        implements Command {

    public record InterestPolicyDto(
            @NotBlank String minRate,
            @NotBlank String maxRate,
            @NotBlank String interestFormula,
            @NotBlank String refundFormula,
            @NotNull Boolean dailyInterest) {}

    public record PenaltyPolicyDto(
            @NotBlank String penaltyRate,
            @NotBlank String deferralInterestRate,
            @NotBlank String formula,
            @NotNull PenaltyPaymentType paymentType) {}

    public record InstallmentPolicyDto(
            @NotNull Period installmentPeriod,
            @NotBlank String installmentFormula,
            @NotBlank String interestComponentFormula,
            @NotNull InstallmentPaymentType paymentType,
            @NotNull Boolean isDefineAutomaticInstallment) {}

    public record GracePeriodPolicyDto(
            @NotNull Integer minGracePeriodDays, @NotNull Integer maxGracePeriodDays, @NotBlank String formula) {}

    public record RepaymentPriorityPolicyDto(
            @NotNull Integer principalPriority,
            @NotNull Integer interestPriority,
            @NotNull Integer penaltyPriority,
            @NotNull Integer commissionPriority,
            @NotNull Integer insurancePriority,
            @NotNull Integer insurancePenaltyPriority,
            @NotNull Boolean hasEqualPriority) {}

    public record RegulatoryCompliancePolicyDto(
            @NotNull Integer overDuePeriod, @NotNull Integer deferralPeriod, @NotNull Integer suspiciousPeriod) {}

    public record CollateralPolicyDto(@NotNull Integer totalPercent, @NotNull Set<CollateralTypeDto> collateralTypes) {}

    public record CollateralTypeDto(@NotBlank String code) {}

    public record LoanArrangementCodeDto(@NotBlank String value) {}

    public record TitleDto(@NotBlank String value) {}

    public record CurrencyTypeDto(@NotBlank String value) {}

    public record AmountRangeDto(@NotNull MoneyDto min, @NotNull MoneyDto max) {}

    public record LoanDurationRangeDto(@NotNull Period minDays, @NotNull Period maxDays) {}

    public record MoneyDto(@NotNull BigDecimal value) {}

    public record EconomicSectorDto(@NotBlank String code) {}

    public record ConfirmTypeDto(@NotBlank String personCode) {}
}
