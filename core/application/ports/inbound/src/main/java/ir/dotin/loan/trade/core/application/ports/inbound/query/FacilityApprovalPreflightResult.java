package ir.dotin.loan.trade.core.application.ports.inbound.query;

import ir.dotin.platform.pangaea.dispatcher.api.query.QueryResult;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.SanctionDetailsDto;

import lombok.Builder;

/**
 * Result of the tx-free manual-approval pre-flight ({@link PrepareFacilityApprovalQuery}). Carries the FCB-resolved
 * sanction details — fetched by the same {@code FetchSanctionDetailsPort} the strategy used to call — as a
 * transport-neutral {@link SanctionDetailsDto}. The controller / saga threads this onto
 * {@code ApproveFacilityCommand.sanctionDetails} so the lean transactional command can rebuild the outbound
 * {@code SanctionDetails} without any further FCB call.
 */
@Builder
public record FacilityApprovalPreflightResult(SanctionDetailsDto sanctionDetails) implements QueryResult {}
