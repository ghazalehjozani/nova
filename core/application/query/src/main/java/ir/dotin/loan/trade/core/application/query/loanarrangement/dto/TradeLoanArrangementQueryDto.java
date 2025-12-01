package ir.dotin.loan.trade.core.application.query.loanarrangement.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ir.dotin.platform.dispatcher.api.query.QueryResult;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;

import lombok.Builder;

public record TradeLoanArrangementQueryDto(
        UUID id,
        Long version,
        @JsonIgnore LocalDateTime createdAt,
        String code,
        TitleEmbDto title,
        boolean active,
        boolean disable,
        CurrencyTypeEmbDto currencyType,
        EconomicSectorEmbDto economicSector,
        AmountRangeEmbDto amountRange,
        PeriodRangeEmbDto durationRange,
        Integer guarantorCount,
        PartyType partyType,
        boolean hasInstallmentCard,
        LifeInsurancePaymentType lifeInsurancePaymentType,
        LoanSecondaryType loanSecondaryType,
        SectionType sectionType,
        InterestPolicyEmbDto interestPolicy,
        PenaltyPolicyEmbDto penaltyPolicy,
        InstallmentPolicyEmbDto installmentPolicy,
        GracePeriodPolicyEmbDto gracePeriodPolicy,
        RepaymentPriorityPolicyEmbDto repaymentPriorityPolicy,
        RegulatoryCompliancePolicyEmbDto regulatoryCompliancePolicy,
        CollateralPolicyEmbDto collateralPolicy,
        UUID previousVersion)
        implements QueryResult {

    public record TitleEmbDto(String value) implements Serializable {}

    public record CurrencyTypeEmbDto(String value) implements Serializable {}

    public record EconomicSectorEmbDto(String code) implements Serializable {}

    public record AmountRangeEmbDto(BigDecimal minAmount, BigDecimal maxAmount, String currency)
            implements Serializable {}

    public record PeriodRangeEmbDto(PeriodEmbDto minPeriod, PeriodEmbDto maxPeriod) implements Serializable {

        public record PeriodEmbDto(Integer years, Integer months, Integer days) implements Serializable {}
    }

    public record InterestPolicyEmbDto(
            BigDecimal baseInterestRate,
            BigDecimal preferentialMinRate,
            BigDecimal preferentialMaxRate,
            FormulaDto interestFormula,
            FormulaDto refundInterestFormula,
            Boolean dailyInterest)
            implements Serializable {}

    public record PenaltyPolicyEmbDto(
            BigDecimal penaltyRate,
            BigDecimal deferralInterestRate,
            FormulaDto penaltyFormula,
            String penaltyPaymentType)
            implements Serializable {}

    public record InstallmentPolicyEmbDto(
            Integer installmentPeriodDays,
            FormulaDto installmentFormula,
            FormulaDto interestComponentFormula,
            String installmentPaymentType)
            implements Serializable {}

    public record GracePeriodPolicyEmbDto(
            Integer minGracePeriodDays, Integer maxGracePeriodDays, FormulaDto gracePeriodFormula)
            implements Serializable {}

    public record RepaymentPriorityPolicyEmbDto(
            Integer installmentMainAmountPriority,
            Integer installmentInterestAmountPriority,
            Integer installmentPenaltyAmountPriority,
            Integer installmentIncomeAmountPriority,
            Integer insuranceAmountPriority,
            Integer insurancePenaltyAmountPriority,
            Boolean hasEqualPriority)
            implements Serializable {}

    public record RegulatoryCompliancePolicyEmbDto(
            Integer overDuePeriod, Integer deferralPeriod, Integer suspiciousPeriod) implements Serializable {}

    public record CollateralPolicyEmbDto(
            Integer totalPercent, Set<CollateralTypeEmbDto> collateralTypes, String collateralCalculationType)
            implements Serializable {

        public record CollateralTypeEmbDto(String code) implements Serializable {}
    }

    @Builder
    public record FormulaDto(String expression, Map<Character, String> fieldMappings) {}
}
