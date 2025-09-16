package ir.dotin.loan.trade.adapters.driven.persistance.converter;

import java.time.Period;

import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.convert.converter.AbstractGenericBidirectionalConverter;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPeriod;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.InstallmentPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.InvalidDomainStateException;
import ir.dotin.loan.trade.adapters.driven.persistance.service.FormulaFieldMappingService;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Component
public class InstallmentPolicyConverter
        extends AbstractGenericBidirectionalConverter<
                InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>, InstallmentPolicyEmb> {

    private final FormulaFieldMappingService formulaMappingService;

    public InstallmentPolicyConverter(FormulaFieldMappingService formulaMappingService) {
        super(
                ResolvableType.forClassWithGenerics(
                        InstallmentPolicy.class,
                        ResolvableType.forClass(TradeLoanParameterProvider.class),
                        ResolvableType.forClass(TradeLoanFacilityFormulaField.class)),
                ResolvableType.forClass(InstallmentPolicyEmb.class));
        this.formulaMappingService = formulaMappingService;
    }

    @Override
    @Nullable
    protected InstallmentPolicyEmb convertAToB(
            @Nullable InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> policy,
            @NonNull TypeDescriptor sourceType,
            @NonNull TypeDescriptor targetType) {
        if (policy == null) return null;

        InstallmentPolicyEmb emb = new InstallmentPolicyEmb();
        emb.setInstallmentPeriodDays(policy.installmentPeriod().value().getDays());
        emb.setInstallmentFormula(formulaMappingService.serializeParameterizedFormula(policy.installmentFormula()));
        emb.setInterestComponentFormula(
                formulaMappingService.serializeParameterizedFormula(policy.interestComponentFormula()));
        emb.setInstallmentPaymentType(policy.installmentPaymentType().name());
        emb.setDefineAutomaticInstallment(policy.isDefineAutomaticInstallment());
        return emb;
    }

    @Override
    @Nullable
    protected InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> convertBToA(
            @Nullable InstallmentPolicyEmb emb,
            @NonNull TypeDescriptor sourceType,
            @NonNull TypeDescriptor targetType) {
        if (emb == null) return null;

        Period period = Period.ofDays(emb.getInstallmentPeriodDays());
        InstallmentPeriod installmentPeriod = InstallmentPeriod.of(period)
                .orElseThrow(() -> new InvalidDomainStateException(
                        "Invalid installment period: " + emb.getInstallmentPeriodDays()));

        var installmentFormula = formulaMappingService.deserializeParameterizedFormula(emb.getInstallmentFormula());
        var interestComponentFormula =
                formulaMappingService.deserializeParameterizedFormula(emb.getInterestComponentFormula());

        InstallmentPaymentType paymentType = InstallmentPaymentType.valueOf(emb.getInstallmentPaymentType());

        return InstallmentPolicy.of(
                        installmentPeriod,
                        installmentFormula,
                        interestComponentFormula,
                        paymentType,
                        Boolean.TRUE.equals(emb.getDefineAutomaticInstallment()))
                .orElseThrow(
                        () -> new InvalidDomainStateException("Cannot create InstallmentPolicy from persisted data"));
    }
}
