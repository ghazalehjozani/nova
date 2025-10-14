package ir.dotin.loan.trade.core.application.ports.outbound.query.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;

public record TradeLoanArrangementQueryDto(
        UUID id,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String createdBy,
        String modifiedBy,
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
        ConfirmTypeEmbDto confirmType,
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
        boolean autoApproval,
        DisbursementMethod disbursementMethod,
        UUID previousVersion)
        implements Serializable {
    /** DTO for {@link ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.TitleEmb} */
    public record TitleEmbDto(String value) implements Serializable {}

    /** DTO for {@link ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CurrencyTypeEmb} */
    public record CurrencyTypeEmbDto(String value) implements Serializable {}

    /** DTO for {@link ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.EconomicSectorEmb} */
    public record EconomicSectorEmbDto(String code, String name) implements Serializable {}

    /** DTO for {@link ir.dotin.platform.adapter.persistence.embeddable.AmountRangeEmb} */
    public record AmountRangeEmbDto(BigDecimal minAmount, BigDecimal maxAmount, String currency)
            implements Serializable {}

    public record PeriodRangeEmbDto(PeriodEmbDto minPeriod, PeriodEmbDto maxPeriod) implements Serializable {

        public record PeriodEmbDto(Integer years, Integer months, Integer days) implements Serializable {}
    }

    public record ConfirmTypeEmbDto(String personCode, String personName) implements Serializable {}

    /** DTO for {@link ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InterestPolicyEmb} */
    public record InterestPolicyEmbDto(
            BigDecimal baseInterestRate,
            BigDecimal preferentialMinRate,
            BigDecimal preferentialMaxRate,
            String interestFormula,
            String refundInterestFormula,
            Boolean dailyInterest)
            implements Serializable {}

    /** DTO for {@link ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.PenaltyPolicyEmb} */
    public record PenaltyPolicyEmbDto(
            BigDecimal penaltyRate, BigDecimal deferralInterestRate, String penaltyFormula, String penaltyPaymentType)
            implements Serializable {}

    /** DTO for {@link ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentPolicyEmb} */
    public record InstallmentPolicyEmbDto(
            Integer installmentPeriodDays,
            String installmentFormula,
            String interestComponentFormula,
            String installmentPaymentType,
            Boolean defineAutomaticInstallment)
            implements Serializable {}

    /** DTO for {@link ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.GracePeriodPolicyEmb} */
    public record GracePeriodPolicyEmbDto(
            Integer minGracePeriodDays, Integer maxGracePeriodDays, String gracePeriodFormula)
            implements Serializable {}

    /** DTO for {@link ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RepaymentPriorityPolicyEmb} */
    public record RepaymentPriorityPolicyEmbDto(
            Integer installmentMainAmountPriority,
            Integer installmentInterestAmountPriority,
            Integer installmentPenaltyAmountPriority,
            Integer installmentIncomeAmountPriority,
            Integer insuranceAmountPriority,
            Integer insurancePenaltyAmountPriority,
            Boolean hasEqualPriority)
            implements Serializable {}

    /** DTO for {@link ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RegulatoryCompliancePolicyEmb} */
    public record RegulatoryCompliancePolicyEmbDto(
            Integer overDuePeriod, Integer deferralPeriod, Integer suspiciousPeriod) implements Serializable {}

    /** DTO for {@link ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CollateralPolicyEmb} */
    public record CollateralPolicyEmbDto(Integer totalPercent, Set<CollateralTypeEmbDto> collateralTypes)
            implements Serializable {
        /** DTO for {@link ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CollateralTypeEmb} */
        public record CollateralTypeEmbDto(String code, String name) implements Serializable {}
    }
}
