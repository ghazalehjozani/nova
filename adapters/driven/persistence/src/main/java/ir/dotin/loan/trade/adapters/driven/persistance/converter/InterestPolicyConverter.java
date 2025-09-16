package ir.dotin.loan.trade.adapters.driven.persistance.converter;

import com.google.common.collect.Range;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.convert.converter.AbstractGenericBidirectionalConverter;
import ir.dotin.platform.commons.domain.vo.Rate;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InterestPolicy;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.InterestPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.InvalidDomainStateException;
import ir.dotin.loan.trade.adapters.driven.persistance.service.FormulaFieldMappingService;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Component
public class InterestPolicyConverter
        extends AbstractGenericBidirectionalConverter<
                InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>, InterestPolicyEmb> {

    private final FormulaFieldMappingService formulaMappingService;

    public InterestPolicyConverter(FormulaFieldMappingService formulaMappingService) {
        super(
                ResolvableType.forClassWithGenerics(
                        InterestPolicy.class,
                        ResolvableType.forClass(TradeLoanParameterProvider.class),
                        ResolvableType.forClass(TradeLoanFacilityFormulaField.class)),
                ResolvableType.forClass(InterestPolicyEmb.class));
        this.formulaMappingService = formulaMappingService;
    }

    @Override
    @Nullable
    protected InterestPolicyEmb convertAToB(
            @Nullable InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> policy,
            @NonNull TypeDescriptor sourceType,
            @NonNull TypeDescriptor targetType) {
        if (policy == null) return null;

        InterestPolicyEmb emb = new InterestPolicyEmb();
        emb.setBaseInterestRate(policy.baseInterestRate().value());

        if (policy.preferentialRangeRate().hasLowerBound()) {
            emb.setPreferentialMinRate(
                    policy.preferentialRangeRate().lowerEndpoint().value());
        }
        if (policy.preferentialRangeRate().hasUpperBound()) {
            emb.setPreferentialMaxRate(
                    policy.preferentialRangeRate().upperEndpoint().value());
        }

        emb.setInterestFormula(formulaMappingService.serializeFormula(policy.interestFormula()));
        emb.setRefundInterestFormula(formulaMappingService.serializeFormula(policy.refundInterestFormula()));
        emb.setDailyInterest(policy.dailyInterest());
        return emb;
    }

    @Override
    @Nullable
    protected InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> convertBToA(
            @Nullable InterestPolicyEmb emb, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        if (emb == null) return null;

        Rate baseRate = Rate.valueOf(emb.getBaseInterestRate())
                .orElseThrow(() ->
                        new InvalidDomainStateException("Invalid base interest rate: " + emb.getBaseInterestRate()));

        Rate minRate = Rate.valueOf(emb.getPreferentialMinRate())
                .orElseThrow(() -> new InvalidDomainStateException(
                        "Invalid min preferential rate: " + emb.getPreferentialMinRate()));

        Rate maxRate = Rate.valueOf(emb.getPreferentialMaxRate())
                .orElseThrow(() -> new InvalidDomainStateException(
                        "Invalid max preferential rate: " + emb.getPreferentialMaxRate()));

        Range<Rate> preferentialRange = Range.closed(minRate, maxRate);

        var interestFormula = formulaMappingService.deserializeInterestFormula(emb.getInterestFormula());
        var refundFormula = formulaMappingService.deserializeInterestFormula(emb.getRefundInterestFormula());

        return InterestPolicy.of(
                        baseRate,
                        preferentialRange,
                        interestFormula,
                        refundFormula,
                        Boolean.TRUE.equals(emb.getDailyInterest()))
                .orElseThrow(() -> new InvalidDomainStateException("Cannot create InterestPolicy from persisted data"));
    }
}
