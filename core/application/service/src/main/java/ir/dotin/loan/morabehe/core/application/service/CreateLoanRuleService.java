package ir.dotin.loan.morabehe.core.application.service;


import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;

public interface CreateLoanRuleService {

    String create(MorabeheLoanRule loanRule);
}
