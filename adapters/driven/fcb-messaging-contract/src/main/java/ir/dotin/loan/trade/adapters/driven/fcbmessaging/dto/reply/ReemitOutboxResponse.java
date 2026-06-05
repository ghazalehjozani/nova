package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

/**
 * Reply for the {@code nova-reemit-outbox} operation. Field names are part of the FCB wire contract — keep them
 * byte-for-byte aligned with the FCB-side reply payload.
 */
public final class ReemitOutboxResponse extends FcbBaseResponse {

    private int reemittedCount;

    private @Nullable String status;

    public int getReemittedCount() {
        return reemittedCount;
    }

    public void setReemittedCount(int reemittedCount) {
        this.reemittedCount = reemittedCount;
    }

    public @Nullable String getStatus() {
        return status;
    }

    public void setStatus(@Nullable String status) {
        this.status = status;
    }
}
