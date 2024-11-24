package ir.dotin.loan.morabehe.core.domain.config.service.impl;


import ir.dotin.loan.baseloan.domain.config.service.AbstractLoanRuleUpdatedService;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.exception.MorabeheLoanRuleException;
import ir.dotin.loan.morabehe.core.domain.config.service.MorabeheLoanRuleUpdateService;
import ir.dotin.platform.ddd.common.annotation.DomainService;

@DomainService
public class MorabeheLoanRuleUpdateServiceImpl extends
        AbstractLoanRuleUpdatedService<MorabeheLoanRule> implements MorabeheLoanRuleUpdateService {

    @Override
    protected void validateIsEnabled(MorabeheLoanRule oldLoanRule) {
        oldLoanRule.validateIsEnable();
    }

    @Override
    protected void validateIsActive(MorabeheLoanRule oldLoanRule) {
        oldLoanRule.validateIsActive();

    }

    @Override
    protected void createLoanRule(MorabeheLoanRule newLoanRule) {
        newLoanRule.createLoanRule();
    }

    @Override
    protected void setPreviousVersion(MorabeheLoanRule newLoanRule, MorabeheLoanRule oldLoanRule) {
        newLoanRule.setPreviousVersion(oldLoanRule.getLoanRule().getId());
    }

    @Override
    protected void markAsDisabled(MorabeheLoanRule oldLoanRule) {
        oldLoanRule.markAsDisabled();

    }

    @Override
    protected void ensureLoanRuleCodeNotChanged(MorabeheLoanRule oldLoanRule,
                                                MorabeheLoanRule newLoanRule) {
        boolean notEqual = !oldLoanRule.getLoanRule().getCode()
                .equals(newLoanRule.getLoanRule().getCode());
        if (notEqual) {
            throw new MorabeheLoanRuleException("error.validation.base.unchangeable", "code");
        }
    }

}
