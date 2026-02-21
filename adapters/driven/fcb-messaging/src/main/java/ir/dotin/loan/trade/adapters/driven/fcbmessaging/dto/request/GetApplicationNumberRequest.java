package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class GetApplicationNumberRequest extends FcbKafkaBaseRequest {

    private final String branchCode;
    private final String loanTypeCode;
    private final String customerNumber;

    public GetApplicationNumberRequest(String branchCode, String loanTypeCode, String customerNumber) {
        super("get-loan-file-number");
        this.branchCode = branchCode;
        this.loanTypeCode = loanTypeCode;
        this.customerNumber = customerNumber;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public String getLoanTypeCode() {
        return loanTypeCode;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }
}
