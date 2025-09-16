package ir.dotin.loan.trade.adapters.driven.persistance.converter;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.convert.converter.AbstractBidirectionalConverter;
import ir.dotin.platform.commons.domain.vo.Rate;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.PenaltyPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PenaltyPaymentType;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.PenaltyPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.InvalidDomainStateException;
import ir.dotin.loan.trade.adapters.driven.persistance.service.FormulaFieldMappingService;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Component
public class PenaltyPolicyConverter
        extends AbstractBidirectionalConverter<
                PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>, PenaltyPolicyEmb> {

    private final FormulaFieldMappingService formulaMappingService;

    public PenaltyPolicyConverter(FormulaFieldMappingService formulaMappingService) {
        super(
                (Class<PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>)
                        (Class<?>) PenaltyPolicy.class,
                PenaltyPolicyEmb.class);
        this.formulaMappingService = formulaMappingService;
    }

    @Override
    @Nullable
    protected PenaltyPolicyEmb convertAToB(
            @Nullable PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> policy) {
        if (policy == null) return null;

        PenaltyPolicyEmb emb = new PenaltyPolicyEmb();
        emb.setPenaltyRate(policy.penaltyRate().value());
        emb.setDeferralInterestRate(policy.deferralInterestRate().value());
        emb.setPenaltyFormula(formulaMappingService.serializeParameterizedFormula(policy.penaltyFormula()));
        emb.setPenaltyPaymentType(policy.penaltyPaymentType().name());

        return emb;
    }

    @Override
    @Nullable
    protected PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> convertBToA(
            @Nullable PenaltyPolicyEmb emb) {
        if (emb == null) return null;

        Rate penaltyRate = Rate.valueOf(emb.getPenaltyRate())
                .orElseThrow(() -> new InvalidDomainStateException("Invalid penalty rate: " + emb.getPenaltyRate()));

        Rate deferralRate = Rate.valueOf(emb.getDeferralInterestRate())
                .orElseThrow(() -> new InvalidDomainStateException(
                        "Invalid deferral interest rate: " + emb.getDeferralInterestRate()));

        var penaltyFormula = formulaMappingService.deserializeParameterizedFormula(emb.getPenaltyFormula());

        PenaltyPaymentType paymentType = PenaltyPaymentType.valueOf(emb.getPenaltyPaymentType());

        return PenaltyPolicy.of(penaltyRate, deferralRate, penaltyFormula, paymentType)
                .orElseThrow(() -> new InvalidDomainStateException("Cannot create PenaltyPolicy from persisted data"));
    }
}
