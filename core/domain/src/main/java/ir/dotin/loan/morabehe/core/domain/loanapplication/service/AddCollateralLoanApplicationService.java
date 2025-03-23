package ir.dotin.loan.morabehe.core.domain.loanapplication.service;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.Collateral;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.Sanction;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;

public interface AddCollateralLoanApplicationService {

    void addCollateral(
            MorabeheLoanApplication baseLoanApplication,
            MorabeheLoanRule baseLoanRule,
            Sanction sanction,
            Collateral collateral);
}
