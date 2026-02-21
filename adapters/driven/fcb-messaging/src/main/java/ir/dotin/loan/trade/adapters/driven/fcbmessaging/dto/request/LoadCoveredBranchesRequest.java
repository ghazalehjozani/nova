package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class LoadCoveredBranchesRequest extends FcbKafkaBaseRequest {

    private final String branchCode;

    public LoadCoveredBranchesRequest(String branchCode) {
        super("load-covered-branches");
        this.branchCode = branchCode;
    }

    public String getBranchCode() {
        return branchCode;
    }
}
