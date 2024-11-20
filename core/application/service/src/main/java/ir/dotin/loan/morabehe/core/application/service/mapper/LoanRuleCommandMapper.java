package ir.dotin.loan.morabehe.core.application.service.mapper;

import ir.dotin.loan.baseloan.domain.baseinfo.valueobject.Currency;
import ir.dotin.loan.baseloan.domain.baseinfo.valueobject.EconomicSector;
import ir.dotin.loan.baseloan.domain.config.valueobject.CollateralPolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.CollateralType;
import ir.dotin.loan.baseloan.domain.config.valueobject.GracePeriodPolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.InstallmentPolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.InterestPolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleCode;
import ir.dotin.loan.baseloan.domain.config.valueobject.PenaltyPolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.RegulatoryCompliancePolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.RepaymentPriorityPolicy;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Formula;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Money;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Range;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Rate;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Title;
import ir.dotin.loan.morabehe.core.application.service.command.CreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule.LoanRuleBuilder;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class LoanRuleCommandMapper {

    public MorabeheLoanRule mapToAggregateRoot(CreateLoanRuleCommand command) {
        if (command == null) {
            return null;
        }
        var interestPolicy = mapInterestPolicy(command);
        var penaltyPolicy = mapPenaltyPolicy(command);
        var installmentPolicy = mapInstallmentPolicy(command);
        var gracePeriodPolicy = mapGracePeriodPolicy(command);
        var repaymentPriorityPolicy = mapRepaymentPriorityPolicy(command);
        var regulatoryCompliancePolicy = mapRegulatoryCompliancePolicy(command);
        var collateralPolicy = mapCollateralPolicy(command);

        var loanRuleBuilder = createLoanRuleBuilder(command)
                .withInterestPolicy(interestPolicy)
                .withPenaltyPolicy(penaltyPolicy)
                .withInstallmentPolicy(installmentPolicy)
                .withGracePeriodPolicy(gracePeriodPolicy)
                .withRepaymentPriorityPolicy(repaymentPriorityPolicy)
                .withRegulatoryCompliancePolicy(regulatoryCompliancePolicy)
                .withCollateralPolicy(collateralPolicy);

        return new MorabeheLoanRule(null, loanRuleBuilder);
    }

    public LoanRuleResponse mapToResponse(MorabeheLoanRule loanRule) {
        return new LoanRuleResponse(loanRule.getId().id());
    }


    private InterestPolicy mapInterestPolicy(CreateLoanRuleCommand command) {
        return new InterestPolicy(
                Rate.of(command.interestPolicy().baseInterestRate()),
                Range.of(
                        Rate.of(command.interestPolicy().preferentialRangeRate().min()),
                        Rate.of(command.interestPolicy().preferentialRangeRate().max())
                ),
                new Formula(command.interestPolicy().interestFormula()),
                new Formula(command.interestPolicy().refundInterestFormula()),
                command.interestPolicy().dailyInterest()
        );
    }

    private PenaltyPolicy mapPenaltyPolicy(CreateLoanRuleCommand command) {
        return new PenaltyPolicy(
                Rate.of(command.penaltyPolicy().penaltyRate()),
                Rate.of(command.penaltyPolicy().deferralInterestRate()),
                new Formula(command.penaltyPolicy().penaltyFormula()),
                command.penaltyPolicy().penaltyPaymentType()
        );
    }

    private InstallmentPolicy mapInstallmentPolicy(CreateLoanRuleCommand command) {
        return new InstallmentPolicy(
                Range.of(
                        Duration.parse(command.installmentPolicy().durationRange().min()),
                        Duration.parse(command.installmentPolicy().durationRange().max())
                ),
                new Formula(command.installmentPolicy().installmentFormula()),
                new Formula(command.installmentPolicy().interestComponentFormula()),
                command.installmentPolicy().paymentType(),
                command.installmentPolicy().isDefineAutomaticInstallment()
        );
    }

    private GracePeriodPolicy mapGracePeriodPolicy(CreateLoanRuleCommand command) {
        return new GracePeriodPolicy(
                Range.of(
                        Duration.parse(command.gracePeriodPolicy().gracePeriodRange().min()),
                        Duration.parse(command.gracePeriodPolicy().gracePeriodRange().max())
                ),
                new Formula(command.gracePeriodPolicy().formula())
        );
    }

    private RepaymentPriorityPolicy mapRepaymentPriorityPolicy(CreateLoanRuleCommand command) {
        return new RepaymentPriorityPolicy(
                command.repaymentPriorityPolicy().installmentMainAmountPriority(),
                command.repaymentPriorityPolicy().installmentInterestAmountPriority(),
                command.repaymentPriorityPolicy().installmentPenaltyAmountPriority(),
                command.repaymentPriorityPolicy().installmentIncomeAmountPriority(),
                command.repaymentPriorityPolicy().insuranceAmountPriority(),
                command.repaymentPriorityPolicy().insurancePenaltyAmountPriority(),
                command.repaymentPriorityPolicy().hasEqualPriority()
        );
    }

    private RegulatoryCompliancePolicy mapRegulatoryCompliancePolicy(
            CreateLoanRuleCommand command) {
        return new RegulatoryCompliancePolicy(
                command.regulatoryCompliancePolicy().overDuePeriod(),
                command.regulatoryCompliancePolicy().deferralPeriod(),
                command.regulatoryCompliancePolicy().suspiciousPeriod()
        );
    }

    private CollateralPolicy mapCollateralPolicy(CreateLoanRuleCommand command) {
        return new CollateralPolicy(
                command.collateralPolicy().collateralTypes().stream()
                        .map(collateralType -> new CollateralType(
                                collateralType.code(),
                                collateralType.name()))
                        .collect(Collectors.toSet()),
                command.collateralPolicy().totalPercent()
        );
    }

    private LoanRuleBuilder createLoanRuleBuilder(CreateLoanRuleCommand command) {
        return new LoanRuleBuilder(new FeatureConfig(Map.of("feat1", false)))
                .withCode(new LoanRuleCode(command.code()))
                .withTitle(new Title(command.title()))
                .withEconomicSectors(
                        command.economicSectors().stream()
                                .map(sectors -> new EconomicSector(sectors.code(), sectors.name()))
                                .collect(Collectors.toSet()))
                .withCurrencies(
                        command.currencies().stream()
                                .map(currency -> new Currency(currency.code(), currency.name()))
                                .collect(Collectors.toSet()))
                .withAmountRange(Range.of(
                        Money.of(command.amountRange().min()),
                        Money.of(command.amountRange().max())))
                .withDurationRange(Range.of(
                        Duration.parse(command.durationRange().min()),
                        Duration.parse(command.durationRange().max())))
                .withGuarantorCount(command.guarantorCount())
                .withCustomerType(command.customerType())
                .withHasInstallmentCard(command.hasInstallmentCard())
                .withConfirmType(command.confirmType())
                .withDisburseType(command.disburseType())
                .withLifeInsurancePaymentType(command.lifeInsurancePaymentType())
                .withRuleDisburseType(command.ruleDisburseType())
                .withLoanSecondaryType(command.loanSecondaryType())
                .withSectionType(command.sectionType());
    }

}
