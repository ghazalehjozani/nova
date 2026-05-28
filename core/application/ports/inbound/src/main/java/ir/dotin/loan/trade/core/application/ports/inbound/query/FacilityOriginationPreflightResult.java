package ir.dotin.loan.trade.core.application.ports.inbound.query;

import java.util.List;

import ir.dotin.platform.pangaea.dispatcher.api.query.QueryResult;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.ResolvedPartyDto;

import lombok.Builder;

/**
 * Result of the tx-free origination pre-flight ({@link PrepareFacilityOriginationQuery}). Carries the FCB-resolved
 * party data — already validated by the same FCB validators the orchestrator used to run — as transport-neutral
 * {@link ResolvedPartyDto}s in declaration order matching the command's parties. The controller / saga thread this list
 * back onto {@code OriginateLoanFacilityCommand.resolvedParties} so the lean transactional command can rebuild the
 * enriched parties without any further FCB calls.
 */
@Builder
public record FacilityOriginationPreflightResult(List<ResolvedPartyDto> parties) implements QueryResult {}
