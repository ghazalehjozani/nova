package ir.dotin.loan.trade.core.application.service.establishloanarrangement.conversion;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RepaymentPriorityPolicy;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;

@Component
public class RepaymentPriorityPolicyDtoToRepaymentPriorityPolicyConverter
        implements Converter<
                EstablishTradeLoanArrangementCommand.RepaymentPriorityPolicyDto, Result<RepaymentPriorityPolicy>> {

    @Override
    public Result<RepaymentPriorityPolicy> convert(
            EstablishTradeLoanArrangementCommand.RepaymentPriorityPolicyDto source) {
        return RepaymentPriorityPolicy.of(
                source.principalPriority(),
                source.interestPriority(),
                source.penaltyPriority(),
                source.commissionPriority(),
                source.insurancePriority(),
                source.insurancePenaltyPriority(),
                source.hasEqualPriority());
    }
}
