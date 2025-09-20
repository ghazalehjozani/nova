package ir.dotin.loan.trade.core.application.ports.driven.command;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.google.common.collect.Range;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PenaltyPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;

public record EstablishTradeLoanArrangementCommand(
        @NotNull UUID uid,
        @NotBlank(message = "{code.required}") String code,
        @NotBlank(message = "{title.required}") String title,
        @NotEmpty(message = "{currencies.required}") Set<String> currencies,
        @NotNull(message = "{amount.range.required}") Range<Money> amountRange,
        @NotNull(message = "{duration.range.required}") Range<Duration> durationRange,
        DisbursementMethod disbursementMethod,
        LifeInsurancePaymentType lifeInsurancePaymentType,
        LoanSecondaryType loanSecondaryType,
        SectionType sectionType,
        PartyType customerType,
        Integer guarantorCount,
        boolean hasInstallmentCard,
        boolean autoApproval,
        @Valid InterestPolicyDto interestPolicy,
        @Valid PenaltyPolicyDto penaltyPolicy,
        @Valid InstallmentPolicyDto installmentPolicy,
        @Valid GracePeriodPolicyDto gracePeriodPolicy,
        @Valid RepaymentPriorityPolicyDto repaymentPriorityPolicy,
        @Valid RegulatoryCompliancePolicyDto regulatoryCompliancePolicy,
        @Valid CollateralPolicyDto collateralPolicy)
        implements Command {

    public record InterestPolicyDto(
            @NotNull(message = "{min.rate.required}") BigDecimal minRate,
            @NotNull(message = "{max.rate.required}") BigDecimal maxRate,
            @NotBlank(message = "{interest.formula.required}") String interestFormula,
            @NotBlank(message = "{refund.formula.required}") String refundFormula,
            @NotNull(message = "{daily.interest.required}") Boolean dailyInterest) {}

    public record PenaltyPolicyDto(
            @NotNull(message = "{penalty.rate.required}") BigDecimal penaltyRate,
            @NotNull(message = "{deferral.interest.rate.required}") BigDecimal deferralInterestRate,
            @NotNull(message = "{penalty.payment.type.required}") PenaltyPaymentType paymentType,
            @NotBlank(message = "{penalty.formula.required}") String formula) {}

    public record InstallmentPolicyDto(
            @NotNull(message = "{min.count.required}") Integer minCount,
            @NotNull(message = "{max.count.required}") Integer maxCount,
            @NotNull(message = "{installment.period.required}") Period installmentPeriod,
            @NotBlank(message = "{installment.formula.required}") String installmentFormula,
            @NotBlank(message = "{interest.component.formula.required}") String interestComponentFormula,
            @NotNull(message = "{installment.payment.type.required}") InstallmentPaymentType paymentType,
            @NotNull(message = "{define.automatic.installment.required}") Boolean isDefineAutomaticInstallment) {}

    public record GracePeriodPolicyDto(
            @NotNull(message = "{min.grace.period.required}") Integer minGracePeriodDays,
            @NotNull(message = "{max.grace.period.required}") Integer maxGracePeriodDays,
            @NotBlank(message = "{grace.period.formula.required}") String formula) {}

    public record RepaymentPriorityPolicyDto(
            @NotNull(message = "{principal.priority.required}") Integer principalPriority,
            @NotNull(message = "{interest.priority.required}") Integer interestPriority,
            @NotNull(message = "{penalty.priority.required}") Integer penaltyPriority,
            @NotNull(message = "{commission.priority.required}") Integer commissionPriority,
            @NotNull(message = "{insurance.priority.required}") Integer insurancePriority,
            @NotNull(message = "{insurance.penalty.priority.required}") Integer insurancePenaltyPriority,
            @NotNull(message = "{has.equal.priority.required}") Boolean hasEqualPriority) {}

    public record RegulatoryCompliancePolicyDto(
            @NotNull(message = "{requires.regulatory.required}") Boolean requiresRegulatory,
            String regulatorId,
            String complianceCode,
            @NotNull(message = "{overdue.period.required}") Integer overDuePeriod,
            @NotNull(message = "{deferral.period.required}") Integer deferralPeriod,
            @NotNull(message = "{suspicious.period.required}") Integer suspiciousPeriod) {}

    public record CollateralPolicyDto(
            @NotEmpty(message = "{collateral.types.required}") List<CollateralTypeDto> collateralTypes,
            @NotNull(message = "{total.percent.required}") Integer totalPercent) {}

    public record CollateralTypeDto(
            @NotBlank(message = "{collateral.type.code.required}") String code,
            @NotBlank(message = "{collateral.type.name.required}") String name) {}
}
