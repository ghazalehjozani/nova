package ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice;

import java.util.UUID;

import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CreateAccountInfo;

public interface AccountServicePort {

    Result<AccountInfo> openAccount(LoanTopic loanTopic, String currencyCode);

    Result<AccountId> openAccount(CreateAccountInfo createAccountInfo);

    Result<AccountNumber> deleteAccount(UUID transactionId, UUID rollBackId, AccountNumber accountNumber);

    Result<AccountNumber> validateAccountNumber(String accountNumber);
}
