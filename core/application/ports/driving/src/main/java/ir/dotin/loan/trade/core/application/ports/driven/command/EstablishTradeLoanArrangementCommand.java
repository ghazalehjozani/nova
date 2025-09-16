package ir.dotin.loan.trade.core.application.ports.driven.command;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import ir.dotin.platform.dispatcher.api.command.Command;

public record EstablishTradeLoanArrangementCommand(
        @NotNull String code,
        @NotNull String title,
        @NotNull Set<String> currencies,
        @NotNull BigDecimal minAmount,
        @NotNull BigDecimal maxAmount,
        @NotNull Duration minDuration,
        @NotNull Duration maxDuration,
        @NotNull String partyType,
        @Positive Integer guarantorCount,
        String confirmType,
        @NotNull String disbursementMethod,
        @NotNull InterestPolicyDto interestPolicy,
        @NotNull PenaltyPolicyDto penaltyPolicy,
        @NotNull InstallmentPolicyDto installmentPolicy,
        @NotNull GracePeriodPolicyDto gracePeriodPolicy,
        @NotNull RepaymentPriorityPolicyDto repaymentPriorityPolicy,
        @NotNull RegulatoryCompliancePolicyDto regulatoryCompliancePolicy,
        @NotNull CollateralPolicyDto collateralPolicy,
        @NotNull String lifeInsurancePaymentType,
        @NotNull String loanSecondaryType,
        @NotNull String sectionType,
        boolean hasInstallmentCard,
        boolean autoApproval,
        UUID uid)
        implements Command {

    public record InterestPolicyDto(
            BigDecimal baseInterestRate,
            BigDecimal minPreferentialRate,
            BigDecimal maxPreferentialRate,
            FormulaDto interestFormula,
            FormulaDto refundInterestFormula,
            boolean dailyInterest) {}

    public record PenaltyPolicyDto(
            BigDecimal penaltyRate,
            BigDecimal deferralInterestRate,
            FormulaDto penaltyFormula,
            String penaltyPaymentType) {}

    public record InstallmentPolicyDto(
            Integer installmentPeriodDays,
            FormulaDto installmentFormula,
            FormulaDto interestComponentFormula,
            String installmentPaymentType,
            boolean isDefineAutomaticInstallment) {}

    public record GracePeriodPolicyDto(
            Integer minGracePeriodDays, Integer maxGracePeriodDays, FormulaDto gracePeriodFormula) {}

    public record FormulaDto(String expression, Map<String, String> fieldMappings) {}

    public record RepaymentPriorityPolicyDto(
            Integer installmentMainAmountPriority,
            Integer installmentInterestAmountPriority,
            Integer installmentPenaltyAmountPriority,
            Integer installmentIncomeAmountPriority,
            Integer insuranceAmountPriority,
            Integer insurancePenaltyAmountPriority,
            boolean hasEqualPriority) {}

    public record RegulatoryCompliancePolicyDto(
            Integer overDuePeriod, Integer deferralPeriod, Integer suspiciousPeriod) {}

    public record CollateralPolicyDto(Set<CollateralTypeDto> collateralTypes, Integer totalPercent) {}

    public record CollateralTypeDto(String code, String name) {}
}
