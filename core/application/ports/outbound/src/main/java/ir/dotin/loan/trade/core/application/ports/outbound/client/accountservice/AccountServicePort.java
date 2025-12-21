package ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice;

import java.util.UUID;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CreateAccountInfo;

public interface AccountServicePort {

    Result<AccountInfo> openAccount(LoanTopic loanTopic);

    Result<AccountId> openAccount(CreateAccountInfo createAccountInfo);

    Result<AccountNumber> deleteAccount(UUID transactionId, UUID rollBackId, AccountNumber accountNumber);

    Result<AccountNumber> validateAccountNumber(String accountNumber);
}
