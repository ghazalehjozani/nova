package ir.dotin.loan.trade.core.domain.loantype.service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Verdict;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.loantype.service.LoanTypeValidationService;
import ir.dotin.loan.baseloan.core.domain.loantype.specification.LoanTypeApplicationAllowedSpecification;
import ir.dotin.loan.baseloan.core.domain.loantype.specification.LoanTypeLoanArrangementExistenceSpecification;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.specification.MandatoryRelationTypeLoanTopicSpecification;

@DomainService
public class TradeLoanTypeValidationService implements LoanTypeValidationService<TradeLoanType, TradeLoanArrangement> {

    @Override
    public Result<Boolean> validateLoanType(TradeLoanType loanType, TradeLoanArrangement loanArrangement) {
        Verdict verdict = new LoanTypeLoanArrangementExistenceSpecification(loanArrangement.getId())
                .and(new LoanTypeApplicationAllowedSpecification())
                .and(new MandatoryRelationTypeLoanTopicSpecification())
                .isSatisfiedBy(loanType);
        return verdict.isSatisfied() ? Result.success(true) : Result.failure(verdict.reasons());
    }

    public Result<Boolean> validateMandatoryRelationTypeLoanTopics(TradeLoanType loanType) {
        Verdict verdict = new MandatoryRelationTypeLoanTopicSpecification().isSatisfiedBy(loanType);
        return verdict.isSatisfied() ? Result.success(true) : Result.failure(verdict.reasons());
    }
}
