package ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice;

import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Article;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CreateAccountInfo;
import ir.dotin.platform.commons.core.Result;

import java.util.List;

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
