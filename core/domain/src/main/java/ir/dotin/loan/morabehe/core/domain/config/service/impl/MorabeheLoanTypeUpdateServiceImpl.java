package ir.dotin.loan.morabehe.core.domain.config.service.impl;

import ir.dotin.platform.ddd.common.annotation.DomainService;
import ir.dotin.loan.baseloan.domain.config.service.AbstractLoanTypeUpdateService;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.service.MorabeheLoanTypeUpdateService;

@DomainService
public class MorabeheLoanTypeUpdateServiceImpl extends AbstractLoanTypeUpdateService<MorabeheLoanType>
        implements MorabeheLoanTypeUpdateService {

    @Override
    protected void validateIsEnabled(MorabeheLoanType oldLoanType) {
        oldLoanType.validateIsEnable();
    }

    @Override
    protected void validateIsActive(MorabeheLoanType oldLoanType) {
        oldLoanType.validateIsActive();
    }

    @Override
    protected void createLoanType(MorabeheLoanType newLoanType) {
        newLoanType.createLoanType();
    }

    @Override
    protected void setPreviousVersion(MorabeheLoanType newLoanType, MorabeheLoanType oldLoanType) {
        newLoanType.setPreviousVersion(oldLoanType.getLoanType().id());
    }

    @Override
    protected void markAsDisabled(MorabeheLoanType oldLoanType) {
        oldLoanType.markAsDisabled();
    }
}
