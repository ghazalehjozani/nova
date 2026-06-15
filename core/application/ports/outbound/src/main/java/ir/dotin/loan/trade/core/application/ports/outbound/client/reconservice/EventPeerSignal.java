package ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice;

import org.jspecify.annotations.Nullable;

/**
 * Durable FCB peer signal for a single forward event uid, returned by the {@code nova-loanfile-recon-state} probe so
 * the reconciliation classifier can decide apply-lost vs lag from progress markers instead of a wall clock (LN-59513).
 *
 * <p>{@code eventUid} is the Nova outbox {@code eventId} (the wire {@code eventUid} header) — the same key FCB stores
 * in its idempotency table and dead-letter table, so it joins a Nova outbox row to FCB's record of having seen the
 * event.
 *
 * @param eventUid the forward event uid this signal describes.
 * @param idempotencyState FCB's idempotency outcome for the uid: {@code COMPLETED} (consumed and confirmed),
 *     {@code IN_PROGRESS} (consuming now), or {@code ABSENT} (no row — never seen, or purged after the dedup TTL).
 * @param dltDead whether FCB holds a {@code DEAD} dead-letter row for this uid.
 * @param dltCategory the dead-letter category when {@code dltDead} is true (e.g. {@code BUSINESS}, {@code POISON},
 *     {@code PERMANENT_*}, {@code TRANSIENT_*}); {@code null} otherwise.
 */
public record EventPeerSignal(
        String eventUid,
        IdempotencyState idempotencyState,
        boolean dltDead,
        @Nullable String dltCategory) {

    public enum IdempotencyState {
        COMPLETED,
        IN_PROGRESS,
        ABSENT
    }
}
