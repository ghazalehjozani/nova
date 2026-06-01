package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;

/**
 * Anti-corruption translation between the Nova {@link FacilityStatus} domain term and the legacy FCB {@code fileStatus}
 * wire term, plus the forward-step ordering used to pick the earliest not-yet-applied event. All legacy/FCB terminology
 * (the {@code REQUEST_LOAN}/{@code APPROVE_LOAN}/… codes) stops at this adapter boundary and never reaches the domain.
 */
final class FacilityReconMapping {

    /** FCB file-status codes, in forward lifecycle order (used to compare "FCB behind Nova"). */
    static final String FCB_REQUEST_LOAN = "REQUEST_LOAN";

    static final String FCB_APPROVE_LOAN = "APPROVE_LOAN";
    static final String FCB_ISSUE_CONTRACT = "ISSUE_CONTRACT";
    static final String FCB_GIVE_LOAN = "GIVE_LOAN";
    static final String FCB_LOAN_REVOKED = "LOAN_REVOKED";

    /**
     * Forward rank of an FCB file status (higher = further along the lifecycle). {@code -1} for an unknown/absent code,
     * so an unknown FCB status is treated as "behind".
     */
    private static final Map<String, Integer> FCB_FORWARD_RANK = Map.of(
            FCB_REQUEST_LOAN, 0,
            FCB_APPROVE_LOAN, 1,
            FCB_ISSUE_CONTRACT, 2,
            FCB_GIVE_LOAN, 3);

    /** Nova terminal states (no further forward progress; reconciliation is terminal-dominant for these). */
    private static final Set<FacilityStatus> TERMINAL = Set.of(
            FacilityStatus.REJECTED,
            FacilityStatus.CLOSED_PAID_OFF,
            FacilityStatus.CLOSED_DEFAULTED,
            FacilityStatus.CANCELLED);

    /** Money-moving / irreversible Nova states — convergence toward these is operator-gated (INV-15). */
    private static final Set<FacilityStatus> MONEY_STATES = Set.of(
            FacilityStatus.PARTIALLY_DISBURSED,
            FacilityStatus.FULLY_DISBURSED,
            FacilityStatus.CLOSED_PAID_OFF,
            FacilityStatus.CLOSED_DEFAULTED);

    private FacilityReconMapping() {}

    static boolean isTerminal(FacilityStatus status) {
        return TERMINAL.contains(status);
    }

    static boolean isMoneyState(FacilityStatus status) {
        return MONEY_STATES.contains(status);
    }

    /**
     * The FCB file status Nova expects to see for a given non-terminal Nova status, or {@code null} if Nova has not yet
     * produced an FCB-visible state for it.
     */
    static @Nullable String expectedFcbFileStatus(FacilityStatus status) {
        return switch (status) {
            case APPLICATION_SUBMITTED, APPROVAL_SUBMITTED -> FCB_REQUEST_LOAN;
            case APPROVED -> FCB_APPROVE_LOAN;
            case ISSUE_CONTRACT -> FCB_ISSUE_CONTRACT;
            case PARTIALLY_DISBURSED, FULLY_DISBURSED -> FCB_GIVE_LOAN;
            default -> null;
        };
    }

    /** Forward rank of an FCB file status; {@code -1} when unknown/null (treated as "behind"). */
    static int fcbRank(@Nullable String fcbFileStatus) {
        if (fcbFileStatus == null) {
            return -1;
        }
        Integer rank = FCB_FORWARD_RANK.get(fcbFileStatus);
        return rank == null ? -1 : rank;
    }

    /**
     * Forward FCB rank an outbox forward event advances the loan file <em>to</em> — used to pick the EARLIEST event FCB
     * has not yet applied relative to its current file status (INV-5). {@code -1} for events that do not advance the
     * FCB file status (e.g. collateral) or unknown types, so they are never chosen as the missing forward step. Matched
     * by token so all disbursement variants (LUMP_SUM / IRREGULAR_TRANCHE / FULLY_DISBURSED) map to GIVE_LOAN.
     */
    static int fcbRankForEvent(@Nullable String eventType) {
        if (eventType == null) {
            return -1;
        }
        if (eventType.contains("DISBURSED")) {
            return fcbRank(FCB_GIVE_LOAN);
        }
        if (eventType.contains("CONTRACT_ISSUED")) {
            return fcbRank(FCB_ISSUE_CONTRACT);
        }
        if (eventType.contains("APPROVAL_SUBMITTED")) {
            return fcbRank(FCB_REQUEST_LOAN);
        }
        if (eventType.contains("APPROVED")) {
            return fcbRank(FCB_APPROVE_LOAN);
        }
        if (eventType.contains("CREATED")) {
            return fcbRank(FCB_REQUEST_LOAN);
        }
        return -1;
    }

    /**
     * True when FCB's observed file status is behind the FCB status Nova expects (FCB is lagging Nova). Returns false
     * when there is no expected FCB status (Nova has nothing FCB-visible yet) or FCB is at/ahead of the expectation.
     */
    static boolean fcbBehind(FacilityStatus novaStatus, @Nullable String observedFcbFileStatus) {
        String expected = expectedFcbFileStatus(novaStatus);
        if (expected == null) {
            return false;
        }
        return fcbRank(observedFcbFileStatus) < fcbRank(expected);
    }

    static Map<String, String> observedDetail(FacilityStatus novaStatus, @Nullable String fcbFileStatus) {
        return Map.of(
                "novaStatus", novaStatus.name(), "fcbFileStatus", fcbFileStatus == null ? "<absent>" : fcbFileStatus);
    }

    /** Whether an FCB file status denotes a revoked/cancelled loan file. */
    static boolean isFcbRevoked(@Nullable String fcbFileStatus) {
        return FCB_LOAN_REVOKED.equals(fcbFileStatus);
    }

    /** FCB inbox operation codes that are safe to re-drive on a DEAD_LETTERED Nova inbox row (INV-7). */
    static final Set<String> REDRIVABLE_INBOX_OPS = Set.of(
            "CANCEL_LOAN_FACILITY",
            "INSTALLMENT_COLLECTION",
            "CLOSE_PAID_OFF",
            "COLLATERAL_UPDATE",
            "LOAN_FACILITY_RESTRUCTURING",
            "COMPENSATE_CANCEL_LOAN_FACILITY",
            "COMPENSATE_INSTALLMENT_COLLECTION",
            "COMPENSATE_CLOSE_PAID_OFF",
            "COMPENSATE_COLLATERAL_UPDATE",
            "COMPENSATE_LOAN_FACILITY_RESTRUCTURING");
}
