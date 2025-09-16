package ir.dotin.loan.trade.adapters.driven.persistance.converter;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.convert.converter.AbstractBidirectionalConverter;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RegulatoryCompliancePolicy;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.RegulatoryCompliancePolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.InvalidDomainStateException;

@Component
public class RegulatoryCompliancePolicyConverter
        extends AbstractBidirectionalConverter<RegulatoryCompliancePolicy, RegulatoryCompliancePolicyEmb> {

    public RegulatoryCompliancePolicyConverter() {
        super(RegulatoryCompliancePolicy.class, RegulatoryCompliancePolicyEmb.class);
    }

    @Override
    @Nullable
    protected RegulatoryCompliancePolicyEmb convertAToB(@Nullable RegulatoryCompliancePolicy policy) {
        if (policy == null) return null;

        RegulatoryCompliancePolicyEmb emb = new RegulatoryCompliancePolicyEmb();
        emb.setOverDuePeriodDays(policy.overDuePeriod());
        emb.setDeferralPeriodDays(policy.deferralPeriod());
        emb.setSuspiciousPeriodDays(policy.suspiciousPeriod());

        return emb;
    }

    @Override
    @Nullable
    protected RegulatoryCompliancePolicy convertBToA(@Nullable RegulatoryCompliancePolicyEmb emb) {
        if (emb == null) return null;

        return RegulatoryCompliancePolicy.of(
                        emb.getOverDuePeriodDays(), emb.getDeferralPeriodDays(), emb.getSuspiciousPeriodDays())
                .orElseThrow(() -> new InvalidDomainStateException(
                        "Cannot create RegulatoryCompliancePolicy from persisted data"));
    }
}
