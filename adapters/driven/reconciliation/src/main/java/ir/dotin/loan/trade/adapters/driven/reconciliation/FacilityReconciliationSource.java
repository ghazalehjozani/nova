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
 * because there is nothing left to converge forward.
 */
@Component
@RequiredArgsConstructor
public class FacilityReconciliationSource implements ReconciliationSource {

    static final ReconciliationType TYPE = ReconciliationType.of("facility-state");

    private final FacilityReconReadPort readPort;

    @Override
    public ReconciliationType type() {
        return TYPE;
    }

    @Override
    public KeyPage nextPage(@Nullable String cursor, int size) {
        List<FacilityReconRow> rows = readPort.pageNonTerminal(cursor, size);
        if (rows.isEmpty()) {
            return KeyPage.empty(TYPE);
        }

        List<OpaqueKey> keys =
                rows.stream().map(row -> OpaqueKey.of(row.facilityId())).toList();

        Watermark next;
        if (rows.size() < size) {
            // partial page → no more keys this cycle
            next = Watermark.last();
        } else {
            FacilityReconRow lastRow = rows.get(rows.size() - 1);
            next = Watermark.at(encodeCursor(lastRow));
        }
        return new KeyPage(TYPE, keys, next);
    }

    /** Cursor encoding mirrored by the persistence adapter: {@code <modifiedAtEpochMs>|<facilityId>}. */
    private static String encodeCursor(FacilityReconRow row) {
        return row.modifiedAtEpochMs() + "|" + row.facilityId();
    }
}
