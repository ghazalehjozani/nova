package ir.dotin.loan.core.domain.loanapplication.service.impl;

import ir.dotin.loan.baseloan.domain.config.entity.loanrule.BaseLoanRule;
import ir.dotin.loan.baseloan.domain.config.entity.loantype.BaseLoanType;
import ir.dotin.loan.baseloan.domain.loanapplication.businessrule.validator.LoanApplicationValidator;
import ir.dotin.loan.baseloan.domain.loanapplication.businessrule.validator.LoanTypeValidator;
import ir.dotin.loan.baseloan.domain.loanapplication.entity.application.BaseLoanApplication;
import ir.dotin.loan.baseloan.domain.loanapplication.service.AbstractCreateLoanApplicationService;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.core.domain.loanapplication.intraction.loader.MorabeheLoanApplicationLoader;
import ir.dotin.loan.core.domain.loanapplication.service.CreateLoanApplicationService;
import ir.dotin.platform.ddd.common.exception.AggregateLoadException;

public class CreateLoanApplicationServiceImpl extends
        AbstractCreateLoanApplicationService<MorabeheLoanApplication, MorabeheLoanRule, MorabeheLoanType> implements
        CreateLoanApplicationService {

    private final MorabeheLoanApplicationLoader morabeheLoanApplicationLoader;

    public CreateLoanApplicationServiceImpl(
            LoanApplicationValidator loanApplicationValidator,
            LoanTypeValidator loanTypeValidator,
            MorabeheLoanApplicationLoader morabeheLoanApplicationLoader) {
        super(loanApplicationValidator, loanTypeValidator);
        this.morabeheLoanApplicationLoader = morabeheLoanApplicationLoader;
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
    protected BaseLoanApplication getBaseLoanApplication(
            MorabeheLoanApplication loanApplicationRoot) {
        return loanApplicationRoot.getLoanApplication();
    }

    @Override
    protected void request(MorabeheLoanApplication loanTypeRoot,
                           ApplicationNumber applicationNumber) {
        loanTypeRoot.request(applicationNumber);
    }

    @Override
    protected void ensureUniqueApplicationNumber(ApplicationNumber applicationNumber) {
        boolean existed = morabeheLoanApplicationLoader.existByApplicationNumber(applicationNumber);
        if (existed) {
            throw new AggregateLoadException("Duplicate application number");
        }
    }

}
