package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import java.util.List;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

import org.jspecify.annotations.Nullable;

public final class BranchCodeListKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable List<BranchCodeDto> branches;

    public @Nullable List<BranchCodeDto> getBranches() {
        return branches;
    }

    public void setBranches(@Nullable List<BranchCodeDto> branches) {
        this.branches = branches;
    }

    public record BranchCodeDto(@Nullable String code) {}
}
