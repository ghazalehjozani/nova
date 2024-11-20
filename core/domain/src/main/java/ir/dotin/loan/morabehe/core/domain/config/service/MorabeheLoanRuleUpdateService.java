package ir.dotin.loan.morabehe.core.domain.config.service;

import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;

public interface MorabeheLoanRuleUpdateService {

    MorabeheLoanRule update(MorabeheLoanRule newLoanRule, MorabeheLoanRule oldLoanRule);
}
