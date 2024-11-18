package ir.dotin.loan.core.domain.loanapplication.service;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.Sanction;
import ir.dotin.loan.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.core.domain.loanapplication.entity.application.MorabeheLoanApplication;

public interface ApproveLoanApplicationService {

    void approved(MorabeheLoanApplication loanApplication,
                  MorabeheLoanRule baseLoanRule,
                  Sanction sanction);

}
