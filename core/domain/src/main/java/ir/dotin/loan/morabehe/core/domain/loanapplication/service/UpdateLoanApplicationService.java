package ir.dotin.loan.morabehe.core.domain.loanapplication.service;

import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;

public interface UpdateLoanApplicationService {

    void update(
            MorabeheLoanApplication newLoanApplication,
            MorabeheLoanApplication oldLoanApplication,
            MorabeheLoanRule loanRule,
            MorabeheLoanType loanType);
}
