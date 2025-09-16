package ir.dotin.loan.trade.adapters.driven.persistance.converter;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.convert.converter.AbstractBidirectionalConverter;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RepaymentPriorityPolicy;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.RepaymentPriorityPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.InvalidDomainStateException;

@Component
public class RepaymentPriorityPolicyConverter
        extends AbstractBidirectionalConverter<RepaymentPriorityPolicy, RepaymentPriorityPolicyEmb> {

    public RepaymentPriorityPolicyConverter() {
        super(RepaymentPriorityPolicy.class, RepaymentPriorityPolicyEmb.class);
    }

    @Override
    @Nullable
    protected RepaymentPriorityPolicyEmb convertAToB(@Nullable RepaymentPriorityPolicy policy) {
        if (policy == null) return null;

        RepaymentPriorityPolicyEmb emb = new RepaymentPriorityPolicyEmb();
        emb.setInstallmentMainAmountPriority(policy.installmentMainAmountPriority());
        emb.setInstallmentInterestAmountPriority(policy.installmentInterestAmountPriority());
        emb.setInstallmentPenaltyAmountPriority(policy.installmentPenaltyAmountPriority());
        emb.setInstallmentIncomeAmountPriority(policy.installmentIncomeAmountPriority());
        emb.setInsuranceAmountPriority(policy.insuranceAmountPriority());
        emb.setInsurancePenaltyAmountPriority(policy.insurancePenaltyAmountPriority());
        emb.setHasEqualPriority(policy.hasEqualPriority());

        return emb;
    }

    @Override
    @Nullable
    protected RepaymentPriorityPolicy convertBToA(@Nullable RepaymentPriorityPolicyEmb emb) {
        if (emb == null) return null;

        return RepaymentPriorityPolicy.of(
                        emb.getInstallmentMainAmountPriority(),
                        emb.getInstallmentInterestAmountPriority(),
                        emb.getInstallmentPenaltyAmountPriority(),
                        emb.getInstallmentIncomeAmountPriority(),
                        emb.getInsuranceAmountPriority(),
                        emb.getInsurancePenaltyAmountPriority(),
                        Boolean.TRUE.equals(emb.getHasEqualPriority()))
                .orElseThrow(() ->
                        new InvalidDomainStateException("Cannot create RepaymentPriorityPolicy from persisted data"));
    }
}
