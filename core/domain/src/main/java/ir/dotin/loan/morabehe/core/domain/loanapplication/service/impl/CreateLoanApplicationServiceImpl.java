package ir.dotin.loan.morabehe.core.domain.loanapplication.service.impl;

import java.time.Clock;

import ir.dotin.platform.ddd.common.annotation.DomainService;
import ir.dotin.platform.ddd.common.exception.AggregateLoadException;
import ir.dotin.loan.baseloan.domain.config.entity.loanrule.BaseLoanRule;
import ir.dotin.loan.baseloan.domain.config.entity.loantype.BaseLoanType;
import ir.dotin.loan.baseloan.domain.loanapplication.businessrule.validator.LoanApplicationValidator;
import ir.dotin.loan.baseloan.domain.loanapplication.businessrule.validator.LoanTypeValidator;
import ir.dotin.loan.baseloan.domain.loanapplication.entity.application.BaseLoanApplication;
import ir.dotin.loan.baseloan.domain.loanapplication.service.AbstractCreateLoanApplicationService;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.intraction.loader.MorabeheLoanApplicationLoader;
import ir.dotin.loan.morabehe.core.domain.loanapplication.service.CreateLoanApplicationService;

@DomainService
public class CreateLoanApplicationServiceImpl
        extends AbstractCreateLoanApplicationService<MorabeheLoanApplication, MorabeheLoanRule, MorabeheLoanType>
        implements CreateLoanApplicationService {

    private final MorabeheLoanApplicationLoader morabeheLoanApplicationLoader;
    private final Clock clock;

    public CreateLoanApplicationServiceImpl(
            LoanApplicationValidator loanApplicationValidator,
            LoanTypeValidator loanTypeValidator,
            MorabeheLoanApplicationLoader morabeheLoanApplicationLoader,
            Clock clock) {
        super(loanApplicationValidator, loanTypeValidator);
        this.morabeheLoanApplicationLoader = morabeheLoanApplicationLoader;
        this.clock = clock;
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
        return loanApplicationRoot.loanApplication();
    }

    @Override
    protected void request(MorabeheLoanApplication loanTypeRoot, ApplicationNumber applicationNumber) {
        loanTypeRoot.request(applicationNumber, clock);
    }

    @Override
    protected void ensureUniqueApplicationNumber(ApplicationNumber applicationNumber) {
        boolean existed = morabeheLoanApplicationLoader.existByApplicationNumber(applicationNumber);
        if (existed) {
            throw new AggregateLoadException("Duplicate application number");
        }
    }
}
