package ir.dotin.loan.morabehe.core.domain.loanapplication.service.impl;

import ir.dotin.loan.baseloan.domain.config.entity.loanrule.BaseLoanRule;
import ir.dotin.loan.baseloan.domain.config.entity.loantype.BaseLoanType;
import ir.dotin.loan.baseloan.domain.loanapplication.businessrule.validator.LoanApplicationValidator;
import ir.dotin.loan.baseloan.domain.loanapplication.businessrule.validator.LoanTypeValidator;
import ir.dotin.loan.baseloan.domain.loanapplication.entity.application.BaseLoanApplication;
import ir.dotin.loan.baseloan.domain.loanapplication.service.AbstractUpdateLoanApplicationService;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.service.UpdateLoanApplicationService;
import ir.dotin.platform.ddd.common.annotation.DomainService;

@DomainService
public class MorabeheLoanApplicationUpdateServiceImpl extends
        AbstractUpdateLoanApplicationService<MorabeheLoanApplication, MorabeheLoanRule, MorabeheLoanType> implements
        UpdateLoanApplicationService {

    public MorabeheLoanApplicationUpdateServiceImpl(
            LoanApplicationValidator loanApplicationValidator,
            LoanTypeValidator loanTypeValidator) {
        super(loanApplicationValidator, loanTypeValidator);
    }

    @Override
    protected void validateUpdate(MorabeheLoanApplication newMorabeheLoanApplication) {
        newMorabeheLoanApplication.validateUpdate();
    }

    @Override
    protected BaseLoanType getBaseLoanType(MorabeheLoanType loanTypeRoot) {
        return loanTypeRoot.getLoanType();
    }

    @Override
    protected BaseLoanRule getBaseLoanRule(MorabeheLoanRule loanRuleRoot) {
        return loanRuleRoot.getLoanRule();
    }

    @Override
    protected BaseLoanApplication getBaseLoanApplication(MorabeheLoanApplication loanApplicationRoot) {
        return loanApplicationRoot.getLoanApplication();
    }

}
