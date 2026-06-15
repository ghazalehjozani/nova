package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

/**
 * Wire element of {@link ReconStateResponse}: FCB's durable peer signal for one forward event uid. Field names are part
 * of the FCB wire contract — keep them byte-for-byte aligned with the FCB-side {@code NovaEventPeerSignal}. The recon
 * adapter maps this onto the port value type {@code EventPeerSignal} (LN-59513).
 */
public final class ReconPeerSignal {

    @SuppressWarnings("NullAway.Init")
    private String eventUid;

    private @Nullable String idempotencyState;

    private @Nullable String dltStatus;

    private @Nullable String dltCategory;

    public String getEventUid() {
        return eventUid;
    }

    public void setEventUid(String eventUid) {
        this.eventUid = eventUid;
    }

    public @Nullable String getIdempotencyState() {
        return idempotencyState;
    }

    public void setIdempotencyState(@Nullable String idempotencyState) {
        this.idempotencyState = idempotencyState;
    }

    public @Nullable String getDltStatus() {
        return dltStatus;
    }

    public void setDltStatus(@Nullable String dltStatus) {
        this.dltStatus = dltStatus;
    }

    public @Nullable String getDltCategory() {
        return dltCategory;
    }

    public void setDltCategory(@Nullable String dltCategory) {
        this.dltCategory = dltCategory;
    }
}
