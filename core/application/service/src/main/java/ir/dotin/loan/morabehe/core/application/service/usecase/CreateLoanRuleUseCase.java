package ir.dotin.loan.morabehe.core.application.service.usecase;


import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;

public interface CreateLoanRuleUseCase {

    MorabeheLoanRule create(MorabeheLoanRule loanRule);
}
