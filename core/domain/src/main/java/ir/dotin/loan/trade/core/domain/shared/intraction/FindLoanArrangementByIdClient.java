package ir.dotin.loan.trade.core.domain.shared.intraction;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

@FunctionalInterface
public interface FindLoanArrangementByIdClient {

    @NonNull
    Result<TradeLoanArrangement> interact(@NonNull LoanArrangementId input);
}
