package ir.dotin.loan.trade.core.application.ports.inbound.query;

import ir.dotin.platform.pangaea.dispatcher.api.query.NonTransactionalQuery;
import ir.dotin.platform.pangaea.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;

import lombok.Builder;

/**
 * Tx-free pre-flight for facility origination. Wraps the full {@link OriginateLoanFacilityCommand} and runs the FCB
 * validations + customer-info loads <em>before</em> the transactional command opens a JPA transaction, so no pooled
 * Hikari connection is held across the (seconds-long) FCB request/reply round-trips.
 *
 * <p>Implements {@link NonTransactionalQuery}: the platform query dispatcher skips its read-only transaction for this
 * query, running the handler with no database transaction (and thus no pooled connection).
 */
@Builder
public record PrepareFacilityOriginationQuery(OriginateLoanFacilityCommand command)
        implements Query<FacilityOriginationPreflightResult>, NonTransactionalQuery {

    @Override
    public Class<FacilityOriginationPreflightResult> getResultType() {
        return FacilityOriginationPreflightResult.class;
    }
}
