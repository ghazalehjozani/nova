package ir.dotin.loan.trade.e2e.fixture;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.boot.test.context.TestComponent;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.CollateralCalculationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ConfirmTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CollateralPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CurrencyTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.EconomicSectorEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.GracePeriodPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InterestPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.PenaltyPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.PeriodRangeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RegulatoryCompliancePolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RepaymentPriorityPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.TitleEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.repository.TradeLoanArrangementJpaRepository;
import ir.dotin.platform.adapter.persistence.embeddable.AmountRangeEmb;
import ir.dotin.platform.adapter.persistence.embeddable.PeriodEmb;

import lombok.RequiredArgsConstructor;

@TestComponent
@RequiredArgsConstructor
public class LoanArrangementTestFixture {

    private final TradeLoanArrangementJpaRepository repository;

    public TradeLoanArrangementEntity createDefaultArrangement() {
        return createArrangement("E2E-ARR-" + UUID.randomUUID().toString().substring(0, 8), e -> {});
    }

    public TradeLoanArrangementEntity createArrangement(
            String code, Consumer<TradeLoanArrangementEntity> customizer) {

        TradeLoanArrangementEntity entity = new TradeLoanArrangementEntity();
        entity.setId(UUID.randomUUID());
        entity.setCode(code);
        entity.setActive(true);
        entity.setDisable(false);

        TitleEmb title = new TitleEmb();
        title.setValue("E2E Test Arrangement");
        entity.setTitle(title);

        CurrencyTypeEmb currency = new CurrencyTypeEmb();
        currency.setValue("IRR");
        entity.setCurrencyType(currency);

        EconomicSectorEmb economicSector = new EconomicSectorEmb();
        economicSector.setCode("2-1");
        entity.setEconomicSector(economicSector);

        AmountRangeEmb amountRange = new AmountRangeEmb();
        amountRange.setMinAmount(new BigDecimal("1000"));
        amountRange.setMaxAmount(new BigDecimal("10000000"));
        amountRange.setCurrency("IRR");
        entity.setAmountRange(amountRange);

        PeriodRangeEmb durationRange = new PeriodRangeEmb();
        PeriodEmb minPeriod = new PeriodEmb();
        minPeriod.setYears(0);
        minPeriod.setMonths(1);
        minPeriod.setDays(0);
        durationRange.setMinPeriod(minPeriod);
        PeriodEmb maxPeriod = new PeriodEmb();
        maxPeriod.setYears(2);
        maxPeriod.setMonths(0);
        maxPeriod.setDays(0);
        durationRange.setMaxPeriod(maxPeriod);
        entity.setDurationRange(durationRange);

        entity.setGuarantorCount(0);
        entity.setPartyType(PartyType.REAL);
        entity.setHasInstallmentCard(false);
        ConfirmTypeEmb ct1 = new ConfirmTypeEmb();
        ct1.setPersonCode("1");
        ConfirmTypeEmb ct2 = new ConfirmTypeEmb();
        ct2.setPersonCode("2");
        ConfirmTypeEmb ct3 = new ConfirmTypeEmb();
        ct3.setPersonCode("3");
        entity.setConfirmTypes(List.of(ct1, ct2, ct3));
        entity.setLifeInsurancePaymentType(LifeInsurancePaymentType.NONE);
        entity.setLoanSecondaryType(LoanSecondaryType.NONE);
        entity.setSectionType(SectionType.NONE);
        entity.setDisbursementType(DisbursementType.PROGRESSIVE);

        InterestPolicyEmb interestPolicy = new InterestPolicyEmb();
        interestPolicy.setBaseInterestRate(new BigDecimal("20.000000"));
        interestPolicy.setPreferentialMinRate(new BigDecimal("-100.000000"));
        interestPolicy.setPreferentialMaxRate(new BigDecimal("100.000000"));
        interestPolicy.setDailyInterest(true);
        entity.setInterestPolicy(interestPolicy);

        PenaltyPolicyEmb penaltyPolicy = new PenaltyPolicyEmb();
        penaltyPolicy.setPenaltyRate(new BigDecimal("2.000000"));
        penaltyPolicy.setDeferralInterestRate(new BigDecimal("6.000000"));
        entity.setPenaltyPolicy(penaltyPolicy);

        InstallmentPolicyEmb installmentPolicy = new InstallmentPolicyEmb();
        installmentPolicy.setInstallmentPeriodDays(30);
        entity.setInstallmentPolicy(installmentPolicy);

        GracePeriodPolicyEmb gracePeriodPolicy = new GracePeriodPolicyEmb();
        gracePeriodPolicy.setMinGracePeriodDays(10);
        gracePeriodPolicy.setMaxGracePeriodDays(30);
        entity.setGracePeriodPolicy(gracePeriodPolicy);

        RepaymentPriorityPolicyEmb repaymentPolicy = new RepaymentPriorityPolicyEmb();
        repaymentPolicy.setInstallmentMainAmountPriority(1);
        repaymentPolicy.setInstallmentInterestAmountPriority(2);
        repaymentPolicy.setInstallmentPenaltyAmountPriority(3);
        repaymentPolicy.setInstallmentIncomeAmountPriority(4);
        repaymentPolicy.setInsuranceAmountPriority(5);
        repaymentPolicy.setInsurancePenaltyAmountPriority(6);
        repaymentPolicy.setHasEqualPriority(false);
        entity.setRepaymentPriorityPolicy(repaymentPolicy);

        RegulatoryCompliancePolicyEmb regulatoryPolicy = new RegulatoryCompliancePolicyEmb();
        regulatoryPolicy.setOverDuePeriod(365);
        regulatoryPolicy.setDeferralPeriod(30);
        regulatoryPolicy.setSuspiciousPeriod(90);
        entity.setRegulatoryCompliancePolicy(regulatoryPolicy);

        CollateralPolicyEmb collateralPolicy = new CollateralPolicyEmb();
        collateralPolicy.setCollateralCalculationType(CollateralCalculationType.BASED_ON_PRINCIPAL);
        collateralPolicy.setTotalPercent(0);
        entity.setCollateralPolicy(collateralPolicy);

        customizer.accept(entity);

        return repository.save(entity);
    }
}
