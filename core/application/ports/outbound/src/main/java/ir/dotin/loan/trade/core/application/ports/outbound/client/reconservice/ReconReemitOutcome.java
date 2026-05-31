package ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice;

/**
 * Outcome of asking FCB to re-emit its outbox event(s) for a facility, as returned by the {@code nova-reemit-outbox}
 * operation. Pure DTO at the outbound-port boundary.
 *
 * @param reemittedCount number of FCB outbox events FCB re-published as a result of the request.
 * @param status FCB-reported status string for the re-emit attempt (e.g. {@code REEMITTED}, {@code NOTHING_TO_REEMIT}).
 */
public record ReconReemitOutcome(int reemittedCount, String status) {}
