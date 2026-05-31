package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

/**
 * Reply for the {@code nova-loanfile-recon-state} operation. Field names are part of the FCB wire contract — keep them
 * byte-for-byte aligned with the FCB-side reply payload.
 */
public final class ReconStateKafkaResponse extends FcbKafkaBaseResponse {

    private boolean exists;

    private @Nullable String fileStatus;

    @SuppressWarnings("NullAway.Init")
    private String manualId;

    private @Nullable Long lastModifiedEpochMs;

    private boolean reachable;

    private @Nullable String outboxRef;

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public @Nullable String getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(@Nullable String fileStatus) {
        this.fileStatus = fileStatus;
    }

    public String getManualId() {
        return manualId;
    }

    public void setManualId(String manualId) {
        this.manualId = manualId;
    }

    public @Nullable Long getLastModifiedEpochMs() {
        return lastModifiedEpochMs;
    }

    public void setLastModifiedEpochMs(@Nullable Long lastModifiedEpochMs) {
        this.lastModifiedEpochMs = lastModifiedEpochMs;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public @Nullable String getOutboxRef() {
        return outboxRef;
    }

    public void setOutboxRef(@Nullable String outboxRef) {
        this.outboxRef = outboxRef;
    }
}
