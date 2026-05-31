package ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

/**
 * Narrow read port for facility-state reconciliation. Pages the facilities a reconciliation source should emit
 * (non-terminal, keyset-ordered by last-modified) and resolves a single facility's reconciliation row by id.
 *
 * <p>Pure interface — no Spring annotations. Implemented by the persistence driven adapter.
 */
public interface FacilityReconReadPort {

    /**
     * Returns the next page of non-terminal facilities ordered by {@code (modifiedAt, id)} for keyset resumption.
     *
     * @param cursor opaque continuation token from the previous page (encodes {@code modifiedAtEpochMs|id}), or
     *     {@code null} to start from the beginning.
     * @param size maximum rows to return.
     */
    List<FacilityReconRow> pageNonTerminal(@Nullable String cursor, int size);

    /** Resolves a single facility's reconciliation row by its id string, if present. */
    Optional<FacilityReconRow> findById(String facilityId);
}
