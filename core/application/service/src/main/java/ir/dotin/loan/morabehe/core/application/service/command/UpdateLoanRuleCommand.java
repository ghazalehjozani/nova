package ir.dotin.loan.morabehe.core.application.service.command;


import ir.dotin.loan.baseloan.domain.config.valueobject.RuleDisburseType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.ConfirmType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.CustomerType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.DisburseType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.LoanSecondaryType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.SectionType;
import ir.dotin.loan.morabehe.core.application.service.command.dto.Currency;
import ir.dotin.loan.morabehe.core.application.service.command.dto.DurationRange;
import ir.dotin.loan.morabehe.core.application.service.command.dto.EconomicSectors;
import ir.dotin.loan.morabehe.core.application.service.command.dto.MoneyRange;

import java.util.Set;
import java.util.UUID;

public record UpdateLoanRuleCommand(UUID oldLoanRuleId,
                                    String code,
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
                                    CreateLoanRuleCommand.InterestPolicy interestPolicy,
                                    CreateLoanRuleCommand.PenaltyPolicy penaltyPolicy,
                                    CreateLoanRuleCommand.InstallmentPolicy installmentPolicy,
                                    CreateLoanRuleCommand.GracePeriodPolicy gracePeriodPolicy,
                                    CreateLoanRuleCommand.RepaymentPriorityPolicy repaymentPriorityPolicy,
                                    CreateLoanRuleCommand.RegulatoryCompliancePolicy regulatoryCompliancePolicy,
                                    CreateLoanRuleCommand.CollateralPolicy collateralPolicy) implements Command {

    @Override
    public String type() {
        return "UpdateLoanRuleCommand";
    }
}
