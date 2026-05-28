package ir.dotin.loan.trade.core.application.ports.inbound.query;

import ir.dotin.platform.pangaea.dispatcher.api.query.NonTransactionalQuery;
import ir.dotin.platform.pangaea.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;

import lombok.Builder;

/**
 * Tx-free pre-flight for MANUAL facility approval. Wraps the {@link ApproveFacilityCommand} and runs the ONE FCB read
 * ({@code FetchSanctionDetailsPort#fetchBySanctionSerial}) <em>before</em> the transactional command opens a JPA
 * transaction, so no pooled Hikari connection is held across the (seconds-long) FCB request/reply round-trip
 * (LN-59412).
 *
 * <p>Implements {@link NonTransactionalQuery}: the platform query dispatcher skips its read-only transaction for this
 * query, running the handler with no database transaction (and thus no pooled connection).
 *
 * <p>Only the manual path (a non-null {@code sanctionSerial} on the command) issues an FCB read; the auto path never
 * dispatches this query.
 */
@Builder
public record PrepareFacilityApprovalQuery(ApproveFacilityCommand command)
        implements Query<FacilityApprovalPreflightResult>, NonTransactionalQuery {

    @Override
    public Class<FacilityApprovalPreflightResult> getResultType() {
        return FacilityApprovalPreflightResult.class;
    }
}
