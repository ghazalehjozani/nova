package ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.servicelayer.api.port.RemoteReadPort;

/**
 * Outbound port for the Nova↔FCB reconciliation corridor. Lets the reconciliation adapter ask FCB for the authoritative
 * legacy loan-file state of a facility ({@code nova-loanfile-recon-state}). The outbox re-emit repair lives on
 * {@link FcbOutboxReemitPort}. Implemented by the FCB Kafka driven adapter.
 *
 * <p>Pure interface — no Spring annotations. Every call is a fresh request/reply with its own {@code eventUid}, so
 * FCB's idempotency cache is bypassed and the probe always reads current state (INV-10).
 */
public interface FcbReconStatePort extends RemoteReadPort {

    /**
     * Reads FCB's current loan-file state for the facility. A communication failure surfaces as a {@link Result}
     * failure; a non-authoritative FCB answer surfaces as {@link ReconLoanFileState#reachable()} == {@code false}.
     */
    Result<ReconLoanFileState> loadReconState(String facilityId);
}
