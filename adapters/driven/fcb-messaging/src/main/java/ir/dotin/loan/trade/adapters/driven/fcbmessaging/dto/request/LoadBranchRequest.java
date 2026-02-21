package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class LoadBranchRequest extends FcbKafkaBaseRequest {

    private final String branchCode;

    public LoadBranchRequest(String branchCode) {
        super("load-branch-nova");
        this.branchCode = branchCode;
    }

    public String getBranchCode() {
        return branchCode;
    }
}
