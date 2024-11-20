package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.domain.config.valueobject.RuleDisburseType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.ConfirmType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.CustomerType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.DisburseType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.LoanSecondaryType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.PaymentType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.PenaltyPaymentType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.SectionType;
import ir.dotin.loan.morabehe.core.application.service.command.dto.CollateralType;
import ir.dotin.loan.morabehe.core.application.service.command.dto.Currency;
import ir.dotin.loan.morabehe.core.application.service.command.dto.DurationRange;
import ir.dotin.loan.morabehe.core.application.service.command.dto.EconomicSectors;
import ir.dotin.loan.morabehe.core.application.service.command.dto.MoneyRange;
import ir.dotin.loan.morabehe.core.application.service.command.dto.RateRange;
import java.util.Set;

public record CreateLoanRuleCommand(String code,
                                    String title,
                                    Boolean active,
                                    Boolean disable,
                                    Set<EconomicSectors> economicSectors,
                                    Set<Currency> currencies,
                                    MoneyRange amountRange,
                                    DurationRange durationRange,
                                    Integer guarantorCount,
                                    CustomerType customerType,
                                    boolean hasInstallmentCard,
                                    ConfirmType confirmType,
                                    DisburseType disburseType,
                                    LifeInsurancePaymentType lifeInsurancePaymentType,
                                    RuleDisburseType ruleDisburseType,
                                    LoanSecondaryType loanSecondaryType,
                                    SectionType sectionType,
                                    InterestPolicy interestPolicy,
                                    PenaltyPolicy penaltyPolicy,
                                    InstallmentPolicy installmentPolicy,
                                    GracePeriodPolicy gracePeriodPolicy,
                                    RepaymentPriorityPolicy repaymentPriorityPolicy,
                                    RegulatoryCompliancePolicy regulatoryCompliancePolicy,
                                    CollateralPolicy collateralPolicy) implements Command {


    @Override
    public String type() {
        return "CreateLoanRuleCommand";
    }

    public record CollateralPolicy(Set<CollateralType> collateralTypes, Integer totalPercent) {

    }

    public record GracePeriodPolicy(DurationRange gracePeriodRange, String formula) {

    }

    public record InstallmentPolicy(DurationRange durationRange,
                                    String installmentFormula,
                                    String interestComponentFormula,
                                    PaymentType paymentType,
                                    boolean isDefineAutomaticInstallment) {

    }

    public record InterestPolicy(Double baseInterestRate,
                                 RateRange preferentialRangeRate,
                                 String interestFormula,
                                 String refundInterestFormula,
                                 boolean dailyInterest) {

    }

    public record PenaltyPolicy(Double penaltyRate,
                                Double deferralInterestRate,
                                String penaltyFormula,
                                PenaltyPaymentType penaltyPaymentType) {

    }

    public record RegulatoryCompliancePolicy(Integer overDuePeriod,
                                             Integer deferralPeriod,
                                             Integer suspiciousPeriod) {

    }

    public record RepaymentPriorityPolicy(Integer installmentMainAmountPriority,
                                          Integer installmentInterestAmountPriority,
                                          Integer installmentPenaltyAmountPriority,
                                          Integer installmentIncomeAmountPriority,
                                          Integer insuranceAmountPriority,
                                          Integer insurancePenaltyAmountPriority,
                                          boolean hasEqualPriority) {

    }

}
