package ir.dotin.loan.morabehe.core.domain.loantype.service;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.loantype.service.LoanTypeValidationService;
import ir.dotin.loan.baseloan.core.domain.loantype.specification.LoanTypeApplicationAllowedSpecification;
import ir.dotin.loan.baseloan.core.domain.loantype.specification.LoanTypeLoanArrangementExistenceSpecification;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.aggregate.MorabeheLoanArrangement;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.vo.MorabeheLoanArrangementId;
import ir.dotin.loan.morabehe.core.domain.loantype.aggregate.MorabeheLoanType;

@DomainService
public class MorabeheLoanTypeValidationService
        implements LoanTypeValidationService<MorabeheLoanType, MorabeheLoanArrangement> {

    @Override
    public Result<Boolean> validateLoanType(MorabeheLoanType loanType, MorabeheLoanArrangement loanArrangement) {
        return new LoanTypeLoanArrangementExistenceSpecification<MorabeheLoanArrangementId, MorabeheLoanType>(
                        loanArrangement.getId())
                .and(new LoanTypeApplicationAllowedSpecification<>())
                .isSatisfiedBy(loanType);
    }
}
