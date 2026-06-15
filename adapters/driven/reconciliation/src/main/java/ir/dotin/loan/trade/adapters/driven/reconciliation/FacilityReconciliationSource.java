package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.reconciliation.api.model.KeyPage;
import ir.dotin.platform.pangaea.reconciliation.api.model.OpaqueKey;
import ir.dotin.platform.pangaea.reconciliation.api.model.ReconciliationType;
import ir.dotin.platform.pangaea.reconciliation.api.model.Watermark;
import ir.dotin.platform.pangaea.reconciliation.api.spi.ReconciliationSource;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconRow;

import lombok.RequiredArgsConstructor;

/**
 * Reconciliation source for the {@code facility-state} type: emits the opaque keys (facility ids) of all non-terminal
 * Nova facilities, keyset-paged by {@code (modifiedAt, id)} so a sweep resumes cleanly after a lost lease.
 *
 * <p>Side-effect-free and cheap, as the orchestrator pages it under a fleet-wide lock. Terminal facilities are excluded
 * because there is nothing left to converge forward. There is no wall-clock detection-settle floor (LN-59513): a
 * just-modified facility is emitted, and the convergence action defers it on a SIGNAL (forward outbox still in-flight)
 * instead of a timer.
 */
@Component
@RequiredArgsConstructor
public class FacilityReconciliationSource implements ReconciliationSource {

    static final ReconciliationType TYPE = ReconciliationType.of("facility-state");

    private final FacilityReconReadPort readPort;
    private final ReconciliationSourceProperties properties;

    @Override
    public ReconciliationType type() {
        return TYPE;
    }

    @Override
    public KeyPage nextPage(@Nullable String cursor, int size) {
        // Cap the engine-requested size by the configured source page size (>=1 guards a misconfigured 0/negative).
        int effectiveSize = Math.clamp(properties.getSourceBatchSize(), 1, size);
        List<FacilityReconRow> rows = readPort.pageNonTerminal(cursor, effectiveSize);
        if (rows.isEmpty()) {
            return KeyPage.empty(TYPE);
        }

        List<OpaqueKey> keys =
                rows.stream().map(row -> OpaqueKey.of(row.facilityId())).toList();

        Watermark next;
        // Compare against the EFFECTIVE size: a full capped page is not "exhausted", it must advance the cursor so the
        // sweep keeps paging (comparing against the un-capped requested size would falsely stop after one page).
        if (rows.size() < effectiveSize) {
            // partial page → no more keys this cycle
            next = Watermark.last();
        } else {
            FacilityReconRow lastRow = rows.getLast();
            next = Watermark.at(encodeCursor(lastRow));
        }
        return new KeyPage(TYPE, keys, next);
    }

    /** Cursor encoding mirrored by the persistence adapter: {@code <modifiedAtEpochMs>|<facilityId>}. */
    private static String encodeCursor(FacilityReconRow row) {
        return row.modifiedAtEpochMs() + "|" + row.facilityId();
    }
}
