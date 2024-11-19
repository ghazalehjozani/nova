package ir.dotin.loan.morabehe.core.domain.loanapplication.service.impl;

import ir.dotin.loan.baseloan.domain.config.entity.loanrule.BaseLoanRule;
import ir.dotin.loan.baseloan.domain.loanapplication.businessrule.validator.SanctionValidator;
import ir.dotin.loan.baseloan.domain.loanapplication.service.AbstractAddCollateralLoanApplicationService;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.CollateralSerial;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.service.AddCollateralLoanApplicationService;

public class AddCollateralLoanApplicationServiceImpl extends
        AbstractAddCollateralLoanApplicationService<MorabeheLoanApplication, MorabeheLoanRule> implements
        AddCollateralLoanApplicationService {


    protected AddCollateralLoanApplicationServiceImpl(SanctionValidator sanctionValidator) {
        super(sanctionValidator);
    }

    @Override
    protected BaseLoanRule getLoanRule(MorabeheLoanRule loanApplicationRoot) {
        return loanApplicationRoot.getLoanRule();
    }

    @Override
    protected void addCollateral(MorabeheLoanApplication loanApplicationRoot, CollateralSerial collateralSerial) {
        loanApplicationRoot.addCollateral(collateralSerial);

    }
}
