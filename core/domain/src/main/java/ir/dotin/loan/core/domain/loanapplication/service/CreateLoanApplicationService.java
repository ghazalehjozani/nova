package ir.dotin.loan.core.domain.loanapplication.service;

import ir.dotin.loan.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.core.domain.loanapplication.entity.application.MorabeheLoanApplication;

public interface CreateLoanApplicationService {

    void create(MorabeheLoanApplication loanApplication, MorabeheLoanRule loanRule,
                MorabeheLoanType loanType);

}
