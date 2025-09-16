package ir.dotin.loan.trade.adapters.driven.persistance.converter;

import java.time.Period;

import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.convert.converter.AbstractGenericBidirectionalConverter;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.GracePeriodPolicy;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.GracePeriodPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.InvalidDomainStateException;
import ir.dotin.loan.trade.adapters.driven.persistance.service.FormulaFieldMappingService;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Component
public class GracePeriodPolicyConverter
        extends AbstractGenericBidirectionalConverter<
                GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>, GracePeriodPolicyEmb> {

    private final FormulaFieldMappingService formulaMappingService;

    public GracePeriodPolicyConverter(FormulaFieldMappingService formulaMappingService) {
        super(
                ResolvableType.forClassWithGenerics(
                        GracePeriodPolicy.class,
                        ResolvableType.forClass(TradeLoanParameterProvider.class),
                        ResolvableType.forClass(TradeLoanFacilityFormulaField.class)),
                ResolvableType.forClass(GracePeriodPolicyEmb.class));
        this.formulaMappingService = formulaMappingService;
    }

    @Override
    @Nullable
    protected GracePeriodPolicyEmb convertAToB(
            @Nullable GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> policy,
            @NonNull TypeDescriptor sourceType,
            @NonNull TypeDescriptor targetType) {
        if (policy == null) return null;

        GracePeriodPolicyEmb emb = new GracePeriodPolicyEmb();
        emb.setMinGracePeriodDays(policy.minGracePeriod().getDays());
        emb.setMaxGracePeriodDays(policy.maxGracePeriod().getDays());
        emb.setGracePeriodFormula(formulaMappingService.serializeParameterizedFormula(policy.gracePeriodFormula()));
        return emb;
    }

    @Override
    @Nullable
    protected GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> convertBToA(
            @Nullable GracePeriodPolicyEmb emb,
            @NonNull TypeDescriptor sourceType,
            @NonNull TypeDescriptor targetType) {
        if (emb == null) return null;

        Period minPeriod = Period.ofDays(emb.getMinGracePeriodDays());
        Period maxPeriod = Period.ofDays(emb.getMaxGracePeriodDays());
        var gracePeriodFormula = formulaMappingService.deserializeParameterizedFormula(emb.getGracePeriodFormula());

        return GracePeriodPolicy.of(minPeriod, maxPeriod, gracePeriodFormula)
                .orElseThrow(
                        () -> new InvalidDomainStateException("Cannot create GracePeriodPolicy from persisted data"));
    }
}
