package ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;

public interface AccountServicePort {

    Result<AccountId> openAccount(LoanTopic loanTopic, BranchCode branchCode);

    Result<TransactionNumber> postTransaction(LoanTransaction loanTransaction);
}
