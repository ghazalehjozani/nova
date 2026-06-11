package ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.servicelayer.api.port.RemotePort;

/**
 * Outbound port for the Nova↔FCB reconciliation corridor. Lets the reconciliation adapter ask FCB for the authoritative
 * legacy loan-file state of a facility ({@code nova-loanfile-recon-state}) and ask FCB to re-emit a missing outbox
 * event ({@code nova-reemit-outbox}). Implemented by the FCB Kafka driven adapter.
 *
 * <p>Pure interface — no Spring annotations. Every call is a fresh request/reply with its own {@code eventUid}, so
 * FCB's idempotency cache is bypassed and the probe always reads current state (INV-10).
 */
public interface FcbReconStatePort extends RemotePort {

    /**
     * Reads FCB's current loan-file state for the facility. A communication failure surfaces as a {@link Result}
     * failure; a non-authoritative FCB answer surfaces as {@link ReconLoanFileState#reachable()} == {@code false}.
     */
    Result<ReconLoanFileState> loadReconState(String facilityId);

    /**
     * Asks FCB to re-emit its outbox event(s) for the facility (used to repair an FCB→Nova lag where FCB never emitted
     * the event Nova is waiting for).
     *
     * @param outboxRef the opaque FCB outbox reference from a prior {@link ReconLoanFileState#outboxRef()}, or
     *     {@code null} to let FCB choose.
     */
    Result<ReconReemitOutcome> reemitOutbox(String facilityId, @Nullable String outboxRef);
}
