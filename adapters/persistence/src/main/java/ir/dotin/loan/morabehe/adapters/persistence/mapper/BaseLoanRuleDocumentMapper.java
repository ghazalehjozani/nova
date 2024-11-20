package ir.dotin.loan.morabehe.adapters.persistence.mapper;

import ir.dotin.loan.adapters.persistance.document.base.CurrencyDocument;
import ir.dotin.loan.adapters.persistance.document.base.DurationRangeDocument;
import ir.dotin.loan.adapters.persistance.document.base.EconomicSectorsDocument;
import ir.dotin.loan.adapters.persistance.document.base.MoneyRangeDocument;
import ir.dotin.loan.adapters.persistance.document.base.RateRangeDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.BaseLoanRuleDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.CollateralPolicyDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.CollateralTypeDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.ConfirmTypeDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.GracePeriodPolicyDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.InstallmentPolicyDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.InterestPolicyDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.PenaltyPolicyDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.RegulatoryCompliancePolicyDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.RepaymentPriorityPolicyDocument;
import ir.dotin.loan.baseloan.domain.baseinfo.valueobject.Currency;
import ir.dotin.loan.baseloan.domain.baseinfo.valueobject.EconomicSector;
import ir.dotin.loan.baseloan.domain.config.valueobject.CollateralPolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.CollateralType;
import ir.dotin.loan.baseloan.domain.config.valueobject.Disable;
import ir.dotin.loan.baseloan.domain.config.valueobject.GracePeriodPolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.InstallmentPolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.InterestPolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleCode;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleId;
import ir.dotin.loan.baseloan.domain.config.valueobject.PenaltyPolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.RegulatoryCompliancePolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.RepaymentPriorityPolicy;
import ir.dotin.loan.baseloan.domain.config.valueobject.RuleDisburseType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Active;
import ir.dotin.loan.baseloan.domain.shared.valueobject.ConfirmType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.CustomerType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.DisburseType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Formula;
import ir.dotin.loan.baseloan.domain.shared.valueobject.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.LoanSecondaryType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Money;
import ir.dotin.loan.baseloan.domain.shared.valueobject.PaymentType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.PenaltyPaymentType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Range;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Rate;
import ir.dotin.loan.baseloan.domain.shared.valueobject.SectionType;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Title;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseLoanRuleDocumentMapper { // It should move to common persistence

    protected BaseLoanRuleDocumentMapper() {
    }


    protected BaseLoanRuleDocument mapBaseLoanRule(LoanRule loanRule) {
        BaseLoanRuleDocument document = new BaseLoanRuleDocument();

        document.setId(loanRule.getId().id());
        document.setCode(loanRule.getCode().code());
        document.setTitle(loanRule.getTitle().value());
        document.setActive(loanRule.isActivated().active());
        document.setDisable(loanRule.isLatestVersion().disable());
        document.setEconomicSectors(mapEconomicSectors(loanRule.getEconomicSectors()));
        document.setCurrencies(mapCurrencies(loanRule.getCurrencies()));
        document.setAmountRange(mapAmountRange(loanRule.getAmountRange()));
        document.setDurationRange(mapDurationRange(loanRule.getDurationRange()));
        document.setGuarantorCount(loanRule.getGuarantorCount());
        document.setCustomerType(loanRule.getCustomerType().name());
        document.setHasInstallmentCard(loanRule.hasInstallmentCard());
        document.setConfirmType(mapConfirmType(loanRule.getConfirmType()));
        document.setDisburseType(loanRule.getDisburseType().name());
        document.setLifeInsurancePaymentType(loanRule.getLifeInsurancePaymentType().name());
        document.setRuleDisburseType(loanRule.getRuleDisburseType().name());
        document.setLoanSecondaryType(loanRule.getLoanSecondaryType().name());
        document.setSectionType(loanRule.getSectionType().name());
        document.setInterestPolicy(mapInterestPolicy(loanRule.getInterestPolicy()));
        document.setPenaltyPolicy(mapPenaltyPolicy(loanRule.getPenaltyPolicy()));
        document.setInstallmentPolicy(mapInstallmentPolicy(loanRule.getInstallmentPolicy()));
        document.setGracePeriodPolicy(mapGracePeriodPolicy(loanRule.getGracePeriodPolicy()));
        document.setRepaymentPriorityPolicy(
                mapRepaymentPriorityPolicy(loanRule.getRepaymentPriorityPolicy()));
        document.setRegulatoryCompliancePolicy(
                mapRegulatoryCompliancePolicy(loanRule.getRegulatoryCompliancePolicy()));
        document.setCollateralPolicy(mapCollateralPolicy(loanRule.getCollateralPolicy()));
        if (loanRule.getPreviousVersion() != null) {
            document.setPreviousVersionId(loanRule.getPreviousVersion().id());
        }

        return document;
    }

    protected LoanRule.LoanRuleBuilder createLoanRuleBuilder(BaseLoanRuleDocument document) {
        LoanRule.LoanRuleBuilder builder = new LoanRule.LoanRuleBuilder(
                new FeatureConfig(Map.of()));

        builder.withId(new LoanRuleId(document.getId()))
                .withCode(new LoanRuleCode(document.getCode()))
                .withTitle(new Title(document.getTitle()))
                .withActivated(new Active(document.isActive()))
                .withDisable(new Disable(document.isDisable()))
                .withEconomicSectors(mapEconomicSectorsFromDocuments(document.getEconomicSectors()))
                .withCurrencies(mapCurrenciesFromCodes(document.getCurrencies()))
                .withAmountRange(Range.of(
                        Money.of(document.getAmountRange().getMin()),
                        Money.of(document.getAmountRange().getMax())))
                .withDurationRange(Range.of(
                        Duration.parse(document.getDurationRange().getMin()),
                        Duration.parse(document.getDurationRange().getMax())))
                .withGuarantorCount(document.getGuarantorCount())
                .withCustomerType(CustomerType.valueOf(document.getCustomerType()))
                .withHasInstallmentCard(document.isHasInstallmentCard())
                .withConfirmType(mapConfirmType(document.getConfirmType()))
                .withDisburseType(DisburseType.valueOf(document.getDisburseType()))
                .withLifeInsurancePaymentType(
                        LifeInsurancePaymentType.valueOf(document.getLifeInsurancePaymentType()))
                .withRuleDisburseType(RuleDisburseType.valueOf(document.getRuleDisburseType()))
                .withLoanSecondaryType(LoanSecondaryType.valueOf(document.getLoanSecondaryType()))
                .withSectionType(SectionType.valueOf(document.getSectionType()))
                .withInterestPolicy(mapInterestPolicyFromDocument(document.getInterestPolicy()))
                .withPenaltyPolicy(mapPenaltyPolicyFromDocument(document.getPenaltyPolicy()))
                .withInstallmentPolicy(
                        mapInstallmentPolicyFromDocument(document.getInstallmentPolicy()))
                .withGracePeriodPolicy(
                        mapGracePeriodPolicyFromDocument(document.getGracePeriodPolicy()))
                .withRepaymentPriorityPolicy(mapRepaymentPriorityPolicyFromDocument(
                        document.getRepaymentPriorityPolicy()))
                .withRegulatoryCompliancePolicy(mapRegulatoryCompliancePolicyFromDocument(
                        document.getRegulatoryCompliancePolicy()))
                .withCollateralPolicy(
                        mapCollateralPolicyFromDocument(document.getCollateralPolicy()))
                .withPreviousVersion(new LoanRuleId(document.getPreviousVersionId()));

        return builder;
    }

    // Mapping Methods for Documents
    private Set<EconomicSectorsDocument> mapEconomicSectors(Set<EconomicSector> economicSectors) {
        return economicSectors.stream()
                .map(es -> new EconomicSectorsDocument(es.code(), es.name()))
                .collect(Collectors.toSet());
    }

    private Set<CurrencyDocument> mapCurrencies(Set<Currency> currencies) {
        return currencies.stream()
                .map(c -> new CurrencyDocument(c.code(), c.name()))
                .collect(Collectors.toSet());
    }

    private MoneyRangeDocument mapAmountRange(Range<Money> amountRange) {
        return new MoneyRangeDocument(amountRange.min().amount(), amountRange.max().amount());
    }

    private DurationRangeDocument mapDurationRange(Range<Duration> durationRange) {
        return new DurationRangeDocument(durationRange.min().toString(),
                                         durationRange.max().toString());
    }

    private InterestPolicyDocument mapInterestPolicy(InterestPolicy interestPolicy) {
        if (interestPolicy == null) {
            return null;
        }
        return new InterestPolicyDocument(
                interestPolicy.baseInterestRate().rate(),
                new RateRangeDocument(
                        interestPolicy.preferentialRangeRate().min().rate(),
                        interestPolicy.preferentialRangeRate().max().rate()
                ),
                interestPolicy.interestFormula().expression(),
                interestPolicy.refundInterestFormula().expression(),
                interestPolicy.dailyInterest()
        );
    }

    private PenaltyPolicyDocument mapPenaltyPolicy(PenaltyPolicy penaltyPolicy) {
        if (penaltyPolicy == null) {
            return null;
        }
        return new PenaltyPolicyDocument(
                penaltyPolicy.penaltyRate().rate(),
                penaltyPolicy.deferralInterestRate().rate(),
                penaltyPolicy.penaltyFormula().expression(),
                penaltyPolicy.penaltyPaymentType().name()
        );
    }

    private InstallmentPolicyDocument mapInstallmentPolicy(InstallmentPolicy installmentPolicy) {
        if (installmentPolicy == null) {
            return null;
        }
        return new InstallmentPolicyDocument(
                new DurationRangeDocument(
                        installmentPolicy.installmentPeriod().min().toString(),
                        installmentPolicy.installmentPeriod().max().toString()
                ),
                installmentPolicy.installmentFormula().expression(),
                installmentPolicy.interestComponentFormula().expression(),
                installmentPolicy.paymentType().name(),
                installmentPolicy.isDefineAutomaticInstallment()
        );
    }

    private GracePeriodPolicyDocument mapGracePeriodPolicy(GracePeriodPolicy gracePeriodPolicy) {
        if (gracePeriodPolicy == null) {
            return null;
        }
        return new GracePeriodPolicyDocument(
                new DurationRangeDocument(
                        gracePeriodPolicy.gracePeriodRange().min().toString(),
                        gracePeriodPolicy.gracePeriodRange().max().toString()
                ),
                gracePeriodPolicy.gracePeriodFormula().expression()
        );
    }

    private RepaymentPriorityPolicyDocument mapRepaymentPriorityPolicy(
            RepaymentPriorityPolicy policy) {
        if (policy == null) {
            return null;
        }
        return new RepaymentPriorityPolicyDocument(
                policy.installmentMainAmountPriority(),
                policy.installmentInterestAmountPriority(),
                policy.installmentPenaltyAmountPriority(),
                policy.installmentIncomeAmountPriority(),
                policy.insuranceAmountPriority(),
                policy.insurancePenaltyAmountPriority(),
                policy.hasEqualPriority()
        );
    }

    private RegulatoryCompliancePolicyDocument mapRegulatoryCompliancePolicy(
            RegulatoryCompliancePolicy policy) {
        if (policy == null) {
            return null;
        }
        return new RegulatoryCompliancePolicyDocument(
                policy.overDuePeriod(),
                policy.deferralPeriod(),
                policy.suspiciousPeriod()
        );
    }

    private CollateralPolicyDocument mapCollateralPolicy(CollateralPolicy collateralPolicy) {
        if (collateralPolicy == null) {
            return null;
        }
        Set<CollateralTypeDocument> collateralTypes = collateralPolicy.collateralTypes().stream()
                .map(ct -> new CollateralTypeDocument(ct.code(), ct.name()))
                .collect(Collectors.toSet());
        return new CollateralPolicyDocument(collateralTypes, collateralPolicy.totalPercent());
    }

    // Mapping Methods from Documents to Domain
    private Set<EconomicSector> mapEconomicSectorsFromDocuments(
            Set<EconomicSectorsDocument> documents) {
        return documents.stream()
                .map(doc -> new EconomicSector(doc.getCode(), doc.getName()))
                .collect(Collectors.toSet());
    }

    private Set<Currency> mapCurrenciesFromCodes(Set<CurrencyDocument> currencyCodes) {
        return currencyCodes.stream()
                .map(c -> new Currency(c.getCode(), c.getName()))
                .collect(Collectors.toSet());
    }

    private InterestPolicy mapInterestPolicyFromDocument(InterestPolicyDocument doc) {
        if (doc == null) {
            return null;
        }
        return new InterestPolicy(
                Rate.of(doc.getBaseInterestRate()),
                Range.of(
                        Rate.of(doc.getPreferentialRangeRate().getMin()),
                        Rate.of(doc.getPreferentialRangeRate().getMax())
                ),
                new Formula(doc.getInterestFormula()),
                new Formula(doc.getRefundInterestFormula()),
                doc.isDailyInterest()
        );
    }

    private ConfirmTypeDocument mapConfirmType(ConfirmType confirmType) {
        return new ConfirmTypeDocument(confirmType.personCode(), confirmType.personName());
    }

    private ConfirmType mapConfirmType(ConfirmTypeDocument document) {
        return new ConfirmType(document.getPersonCode(), document.getPersonName());
    }

    private PenaltyPolicy mapPenaltyPolicyFromDocument(PenaltyPolicyDocument doc) {
        if (doc == null) {
            return null;
        }
        return new PenaltyPolicy(
                Rate.of(doc.getPenaltyRate()),
                Rate.of(doc.getDeferralInterestRate()),
                new Formula(doc.getPenaltyFormula()),
                PenaltyPaymentType.valueOf(doc.getPenaltyPaymentType())
        );
    }

    private InstallmentPolicy mapInstallmentPolicyFromDocument(InstallmentPolicyDocument doc) {
        if (doc == null) {
            return null;
        }
        return new InstallmentPolicy(
                Range.of(
                        Duration.parse(doc.getDurationRange().getMin()),
                        Duration.parse(doc.getDurationRange().getMax())
                ),
                new Formula(doc.getInstallmentFormula()),
                new Formula(doc.getInterestComponentFormula()),
                PaymentType.valueOf(doc.getPaymentType()),
                doc.isDefineAutomaticInstallment()
        );
    }

    private GracePeriodPolicy mapGracePeriodPolicyFromDocument(GracePeriodPolicyDocument doc) {
        if (doc == null) {
            return null;
        }
        return new GracePeriodPolicy(
                Range.of(
                        Duration.parse(doc.getGracePeriodRange().getMin()),
                        Duration.parse(doc.getGracePeriodRange().getMax())
                ),
                new Formula(doc.getFormula())
        );
    }

    private RepaymentPriorityPolicy mapRepaymentPriorityPolicyFromDocument(
            RepaymentPriorityPolicyDocument doc) {
        if (doc == null) {
            return null;
        }
        return new RepaymentPriorityPolicy(
                doc.getInstallmentMainAmountPriority(),
                doc.getInstallmentInterestAmountPriority(),
                doc.getInstallmentPenaltyAmountPriority(),
                doc.getInstallmentIncomeAmountPriority(),
                doc.getInsuranceAmountPriority(),
                doc.getInsurancePenaltyAmountPriority(),
                doc.isHasEqualPriority()
        );
    }

    private RegulatoryCompliancePolicy mapRegulatoryCompliancePolicyFromDocument(
            RegulatoryCompliancePolicyDocument doc) {
        if (doc == null) {
            return null;
        }
        return new RegulatoryCompliancePolicy(
                doc.getOverDuePeriod(),
                doc.getDeferralPeriod(),
                doc.getSuspiciousPeriod()
        );
    }

    private CollateralPolicy mapCollateralPolicyFromDocument(CollateralPolicyDocument doc) {
        if (doc == null) {
            return null;
        }
        Set<CollateralType> collateralTypes = doc.getCollateralTypes().stream()
                .map(ctDoc -> new CollateralType(ctDoc.getCode(), ctDoc.getName()))
                .collect(Collectors.toSet());
        return new CollateralPolicy(collateralTypes, doc.getTotalPercent());
    }


}
