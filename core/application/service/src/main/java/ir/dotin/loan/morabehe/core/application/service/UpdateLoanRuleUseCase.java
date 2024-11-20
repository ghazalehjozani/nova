package ir.dotin.loan.morabehe.core.application.service;

import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;

public interface UpdateLoanRuleUseCase {

    String update(MorabeheLoanRule newMorabeheLoanRule, MorabeheLoanRule oldMorabeheLoanRule);
}
