package ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice;

import java.util.List;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Article;

public interface AccountServicePort {

    Result<AccountInfo> openAccount(LoanTopic loanTopic);

    Result<TransactionNumber> cancelTransferMoney(
            String transactionId,
            TransactionNumber transactionNumber,
            List<Article> articles,
            BranchCode branchCode,
            FailureReason failureReason);
}
