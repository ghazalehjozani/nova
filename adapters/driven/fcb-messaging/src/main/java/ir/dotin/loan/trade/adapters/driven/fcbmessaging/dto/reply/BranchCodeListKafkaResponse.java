package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

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
