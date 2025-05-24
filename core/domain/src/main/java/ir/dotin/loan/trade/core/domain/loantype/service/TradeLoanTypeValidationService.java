package ir.dotin.loan.trade.core.domain.loantype.service;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.loantype.service.LoanTypeValidationService;
import ir.dotin.loan.baseloan.core.domain.loantype.specification.LoanTypeApplicationAllowedSpecification;
import ir.dotin.loan.baseloan.core.domain.loantype.specification.LoanTypeLoanArrangementExistenceSpecification;
import ir.dotin.loan.trade.core.domain.loanarrangement.aggregate.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loantype.aggregate.TradeLoanType;

@DomainService
public class TradeLoanTypeValidationService implements LoanTypeValidationService<TradeLoanType, TradeLoanArrangement> {

    @Override
    public Result<Boolean> validateLoanType(TradeLoanType loanType, TradeLoanArrangement loanArrangement) {
        return new LoanTypeLoanArrangementExistenceSpecification<TradeLoanArrangementId, TradeLoanType>(
                        loanArrangement.getId())
                .and(new LoanTypeApplicationAllowedSpecification<>())
                .isSatisfiedBy(loanType);
    }
}
