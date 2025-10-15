package ir.dotin.loan.trade.core.application.ports.driven.client.customerService;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;

public interface CustomerServicePort {

    Result<CustomerInfo> getCustomerInfo(String customerNumber);

    Result<TransactionNumber> issueDocument(LoanTransaction loanTransaction);

    Result<AccountId> openAccount(LoanTopic loanTopic, BranchCode branchCode);
}
