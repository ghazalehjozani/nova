package ir.dotin.loan.morabehe.core.domain.loanapplication.service.impl;

import ir.dotin.platform.ddd.common.annotation.DomainService;
import ir.dotin.loan.baseloan.domain.config.entity.loanrule.BaseLoanRule;
import ir.dotin.loan.baseloan.domain.loanapplication.businessrule.validator.SanctionValidator;
import ir.dotin.loan.baseloan.domain.loanapplication.entity.application.BaseLoanApplication;
import ir.dotin.loan.baseloan.domain.loanapplication.service.AbstractApproveLoanApplicationService;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.Sanction;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.service.ApproveLoanApplicationService;

@DomainService
public class ApproveLoanApplicationServiceImpl
        extends AbstractApproveLoanApplicationService<MorabeheLoanApplication, MorabeheLoanRule>
        implements ApproveLoanApplicationService {

    public ApproveLoanApplicationServiceImpl(SanctionValidator sanctionValidator) {
        super(sanctionValidator);
    }

    @Override
    protected void approveApplication(MorabeheLoanApplication loanApplicationRoot, Sanction sanction) {
        loanApplicationRoot.approve(sanction.serial());
    }

    @Override
    protected BaseLoanApplication getLoanApplication(MorabeheLoanApplication loanApplicationRoot) {
        return loanApplicationRoot.loanApplication();
    }

    @Override
    protected BaseLoanRule getLoanRule(MorabeheLoanRule loanApplicationRoot) {
        return loanApplicationRoot.getLoanRule();
    }
}
