package ir.dotin.loan.morabehe.core.application.service.usecase;

import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import java.util.UUID;

public interface UpdateLoanRuleUseCase {

    MorabeheLoanRule update(UUID loanRuleId, MorabeheLoanRule newMorabeheLoanRule);
}
