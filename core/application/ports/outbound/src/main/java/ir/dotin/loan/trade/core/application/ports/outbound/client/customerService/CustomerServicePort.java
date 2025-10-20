package ir.dotin.loan.trade.core.application.ports.outbound.client.customerService;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;

public interface CustomerServicePort {

    Result<Party> loadCustomerInfo(String customerNumber, CustomerInfoLoadOptions options);

    Result<TransactionNumber> issueDocument(LoanTransaction loanTransaction);

    Result<AccountId> openAccount(LoanTopic loanTopic, BranchCode branchCode);
}
