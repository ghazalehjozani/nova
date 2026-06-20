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
     * Returns the next page of non-terminal facilities ordered by {@code (modifiedAt, id)} for keyset resumption. There
     * is no wall-clock settling floor (LN-59513): a just-modified facility is emitted and the convergence action defers
     * it on a signal (forward outbox still in-flight) rather than a timer.
     *
     * @param cursor opaque continuation token from the previous page (encodes {@code modifiedAtEpochMs|id}), or
     *     {@code null} to start from the beginning.
     * @param size maximum rows to return.
     */
    List<FacilityReconRow> pageNonTerminal(@Nullable String cursor, int size);

    /**
     * Resolves a single facility's reconciliation row by its id string, if present, WITHOUT the (extra-query) guarantor
     * set. Equivalent to {@link #findById(String, boolean)} with {@code includeGuarantors=false}.
     */
    default Optional<FacilityReconRow> findById(String facilityId) {
        return findById(facilityId, false);
    }

    /**
     * Resolves a single facility's reconciliation row by its id string, if present. When
     * {@code includeGuarantors=true}, the row also carries Nova's current guarantor set (an extra read used only by the
     * guarantor-drift detector); when {@code false}, the guarantor set is left empty and the extra query is skipped, so
     * a recon probe pays nothing for the guarantor-drift feature while it is dark.
     */
    Optional<FacilityReconRow> findById(String facilityId, boolean includeGuarantors);
}
