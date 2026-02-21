package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.util.List;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class ValidateAssuranceRequest extends FcbKafkaBaseRequest {

    private final List<String> assuranceSerials;
    private final List<Long> usedCosts;
    private final List<String> branchCodes;

    public ValidateAssuranceRequest(List<String> assuranceSerials, List<Long> usedCosts, List<String> branchCodes) {
        super("validate-add-assurance-to-file");
        this.assuranceSerials = assuranceSerials;
        this.usedCosts = usedCosts;
        this.branchCodes = branchCodes;
    }

    public List<String> getAssuranceSerials() {
        return assuranceSerials;
    }

    public List<Long> getUsedCosts() {
        return usedCosts;
    }

    public List<String> getBranchCodes() {
        return branchCodes;
    }
}
