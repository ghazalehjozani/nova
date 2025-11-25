package ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice;

import java.util.List;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.*;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Article;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CreateAccountInfo;

public interface AccountServicePort {

    Result<AccountInfo> openAccount(LoanTopic loanTopic);

    Result<TransactionNumber> cancelTransferMoney(
            String transactionId,
            TransactionNumber transactionNumber,
            List<Article> articles,
            BranchCode branchCode,
            FailureReason failureReason);

    Result<AccountId> openAccount(CreateAccountInfo createAccountInfo);
}
