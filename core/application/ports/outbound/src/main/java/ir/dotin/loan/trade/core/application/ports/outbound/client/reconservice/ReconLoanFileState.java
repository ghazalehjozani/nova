package ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice;

import org.jspecify.annotations.Nullable;

/**
 * Nova-side view of the FCB legacy loan-file state for one facility, as returned by the
 * {@code nova-loanfile-recon-state} operation. Pure DTO at the outbound-port boundary — no domain meaning is attached
 * here; the reconciliation probe in the recon adapter interprets these fields against the Nova {@code FacilityStatus}.
 *
 * @param exists whether FCB has a loan-file row for the facility at all.
 * @param fileStatus the legacy FCB file status code (e.g. {@code REQUEST_LOAN}, {@code APPROVE_LOAN},
 *     {@code ISSUE_CONTRACT}, {@code GIVE_LOAN}, {@code LOAN_REVOKED}); {@code null} when {@code exists} is false.
 * @param manualId the legacy FCB manual/file identifier echoed for traceability.
 * @param lastModifiedEpochMs FCB-side last-modified instant in epoch milliseconds; {@code null} when unknown.
 * @param reachable whether FCB answered authoritatively. {@code false} signals the read was inconclusive (peer down /
 *     timeout) and the probe must yield {@code UNKNOWN}, never {@code ORPHAN}.
 * @param outboxRef opaque FCB outbox reference used to re-emit a missing FCB→Nova event; {@code null} when none.
 */
public record ReconLoanFileState(
        boolean exists,
        @Nullable String fileStatus,
        String manualId,
        @Nullable Long lastModifiedEpochMs,
        boolean reachable,
        @Nullable String outboxRef) {}
