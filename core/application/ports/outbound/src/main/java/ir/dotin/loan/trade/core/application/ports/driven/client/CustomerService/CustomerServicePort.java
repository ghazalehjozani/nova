package ir.dotin.loan.trade.core.application.ports.driven.client.CustomerService;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;

public interface CustomerServicePort {

    Result<CustomerInfo> getCustomerInfo(Party customerInfo);
    //
    //        Result<IssueDocumentResponse> issueDocument(
    //                String comment,
    //                String item,
    //                String itemComment,
    //                String branchCode
    //        );
    //
    Result<AccountId> openAccount(LoanTopic loanTopic, BranchCode branchCode);
}
