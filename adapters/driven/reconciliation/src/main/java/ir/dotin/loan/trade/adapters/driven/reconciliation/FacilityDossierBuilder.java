package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.outbox.api.MessageStatus;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxRecordView;
import ir.dotin.platform.pangaea.reconciliation.api.model.Confidence;
import ir.dotin.platform.pangaea.reconciliation.api.model.FcbSnapshot;
import ir.dotin.platform.pangaea.reconciliation.api.model.NovaSnapshot;
import ir.dotin.platform.pangaea.reconciliation.api.model.OperatorDossier;
import ir.dotin.platform.pangaea.reconciliation.api.model.OutboxRowSummary;
import ir.dotin.platform.pangaea.reconciliation.api.model.Precondition;
import ir.dotin.platform.pangaea.reconciliation.api.model.ProposedRemediation;
import ir.dotin.platform.pangaea.reconciliation.api.model.RemediationKind;
import ir.dotin.platform.pangaea.reconciliation.api.model.RootCause;
import ir.dotin.platform.pangaea.reconciliation.api.model.SafetyTier;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.EventPeerSignal;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconGuarantor;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconLoanFileState;

final class FacilityDossierBuilder {

    private static final String SEP = "|";
    private static final String BLAST_RADIUS = "single facility";

    OperatorDossier build(
            FacilityStatus novaStatus,
            ReconLoanFileState fcb,
            List<OutboxRecordView> forwardOutboxRows,
            @Nullable String workflowState,
            FacilityClassification classification,
            @Nullable String fcbBusinessError) {
        Objects.requireNonNull(novaStatus, "novaStatus");
        Objects.requireNonNull(fcb, "fcb");
        Objects.requireNonNull(classification, "classification");
        List<OutboxRecordView> rows = List.copyOf(forwardOutboxRows);

        NovaSnapshot novaSnapshot = novaSnapshot(novaStatus, rows, workflowState);
        FcbSnapshot fcbSnapshot = new FcbSnapshot(fcb.reachable(), fcb.exists(), fcb.fileStatus(), fcbBusinessError);

        ProposedRemediation recommended = recommendation(classification, fcb, rows);
        List<ProposedRemediation> alternatives = alternatives(recommended.kind());

        String hash = computeHash(classification.rootCause(), fcb, novaStatus, rows, recommended.kind());

        return new OperatorDossier(
                classification.rootCause(),
                classification.confidence(),
                novaSnapshot,
                fcbSnapshot,
                classification.evidence(),
                recommended,
                alternatives,
                hash);
    }

    /**
     * Dossier for a guarantor-drift divergence that has no PROCESSED guarantors-changed event to auto-re-drive (so it
     * escalates to an operator). Surfaces both guarantor sets in the evidence so the operator can see Nova's vs FCB's
     * view. Rooted at {@code UNKNOWN} (the recon-api {@code RootCause} enum has no dedicated guarantor-drift value)
     * with a {@code MANUAL_DATA_FIX} recommendation, since there is no stored event to replay.
     */
    OperatorDossier buildGuarantorDriftDossier(
            FacilityStatus novaStatus,
            ReconLoanFileState fcb,
            List<ReconGuarantor> novaGuarantors,
            @Nullable String workflowState) {
        Objects.requireNonNull(novaStatus, "novaStatus");
        Objects.requireNonNull(fcb, "fcb");

        List<String> evidence = new ArrayList<>();
        evidence.add("divergence=guarantor-drift");
        evidence.add("nova.guarantors=" + renderGuarantors(novaGuarantors));
        evidence.add("fcb.guarantors=" + renderGuarantors(fcb.guarantors()));

        NovaSnapshot novaSnapshot = new NovaSnapshot(novaStatus.name(), List.of(), List.of(), workflowState);
        FcbSnapshot fcbSnapshot = new FcbSnapshot(fcb.reachable(), fcb.exists(), fcb.fileStatus(), null);

        ProposedRemediation recommended = new ProposedRemediation(
                RemediationKind.MANUAL_DATA_FIX,
                SafetyTier.MANUAL_ONLY,
                false,
                List.of(),
                "no stored guarantors-changed event to re-drive — reconcile guarantors manually",
                false,
                BLAST_RADIUS,
                List.of(new Precondition(
                        "guarantors-changed-event-present", false, "no PROCESSED guarantors-changed")));

        return new OperatorDossier(
                RootCause.UNKNOWN, Confidence.LOW, novaSnapshot, fcbSnapshot, evidence, recommended, List.of(), "");
    }

    private static String renderGuarantors(List<ReconGuarantor> guarantors) {
        if (guarantors.isEmpty()) {
            return "[]";
        }
        return guarantors.stream()
                .map(g -> g.customerNumber() + "="
                        + (g.guaranteePercentage() == null
                                ? "?"
                                : g.guaranteePercentage().toPlainString()))
                .sorted()
                .collect(Collectors.joining(",", "[", "]"));
    }

    private NovaSnapshot novaSnapshot(
            FacilityStatus novaStatus, List<OutboxRecordView> rows, @Nullable String workflowState) {
        List<String> lastForwardEvents = rows.stream()
                .sorted(Comparator.comparingLong(FacilityDossierBuilder::sequenceOf))
                .map(row -> nullSafe(row.eventType()))
                .toList();
        List<OutboxRowSummary> outboxRows = rows.stream()
                .map(row -> new OutboxRowSummary(
                        eventIdOf(row),
                        nullSafe(row.eventType()),
                        statusNameOf(row),
                        row.status() == MessageStatus.PROCESSED,
                        false,
                        row.sequenceNumber()))
                .toList();
        return new NovaSnapshot(novaStatus.name(), lastForwardEvents, outboxRows, workflowState);
    }

    private ProposedRemediation recommendation(
            FacilityClassification classification, ReconLoanFileState fcb, List<OutboxRecordView> rows) {
        return switch (classification.recommendedKind()) {
            case REPLAY_FORWARD -> replayForward(classification, fcb, rows);
            case REVERSE_NOVA -> reverseNova(classification, fcb);
            case MANUAL_DATA_FIX -> manualDataFix(classification);
            case NONE -> ProposedRemediation.none();
        };
    }

    private ProposedRemediation replayForward(
            FacilityClassification classification, ReconLoanFileState fcb, List<OutboxRecordView> rows) {
        List<String> keys = rows.stream()
                .filter(row -> row.status() == MessageStatus.PROCESSED)
                .sorted(Comparator.comparingLong(FacilityDossierBuilder::sequenceOf))
                .map(FacilityDossierBuilder::eventIdOf)
                .toList();
        int n = keys.size();
        boolean fcbSaw = fcbSawForward(fcb, rows);
        List<Precondition> preconditions = List.of(
                new Precondition("fcb-apply-confirmed", fcbSaw, "fcbSawForwardSignal=" + fcbSaw),
                new Precondition("fcb-effect-absent", !fcb.exists(), "fcb.exists=" + fcb.exists()),
                new Precondition("forward-outbox-processed", n > 0, "processed=" + n),
                new Precondition(
                        "high-confidence",
                        classification.confidence() == Confidence.HIGH,
                        classification.confidence().name()));
        return new ProposedRemediation(
                RemediationKind.REPLAY_FORWARD,
                classification.safetyTier(),
                true,
                keys,
                "re-deliver " + n + " forward event(s) to FCB",
                false,
                BLAST_RADIUS,
                preconditions);
    }

    private ProposedRemediation reverseNova(FacilityClassification classification, ReconLoanFileState fcb) {
        List<Precondition> preconditions = List.of(
                new Precondition("fcb-absent", !fcb.exists(), "fcb.exists=" + fcb.exists()),
                new Precondition("no-forward-outbox", true, "no replayable forward outbox"));
        return new ProposedRemediation(
                RemediationKind.REVERSE_NOVA,
                classification.safetyTier(),
                false,
                List.of(),
                "reverse nova facility to match FCB (manual)",
                false,
                BLAST_RADIUS,
                preconditions);
    }

    private ProposedRemediation manualDataFix(FacilityClassification classification) {
        List<Precondition> preconditions = List.of(new Precondition(
                preconditionNameFor(classification.rootCause()),
                false,
                "rootCause=" + classification.rootCause().name()));
        return new ProposedRemediation(
                RemediationKind.MANUAL_DATA_FIX,
                SafetyTier.MANUAL_ONLY,
                false,
                List.of(),
                "manual data reconciliation required",
                false,
                BLAST_RADIUS,
                preconditions);
    }

    private static String preconditionNameFor(RootCause rootCause) {
        return switch (rootCause) {
            case PARTIAL_APPLY -> "fcb-rank-mismatch";
            case FCB_BUSINESS_REJECT -> "fcb-business-reject";
            default -> "manual-investigation";
        };
    }

    private List<ProposedRemediation> alternatives(RemediationKind recommendedKind) {
        if (recommendedKind == RemediationKind.REPLAY_FORWARD || recommendedKind == RemediationKind.REVERSE_NOVA) {
            return List.of(new ProposedRemediation(
                    RemediationKind.MANUAL_DATA_FIX,
                    SafetyTier.MANUAL_ONLY,
                    false,
                    List.of(),
                    "manual data reconciliation required",
                    false,
                    BLAST_RADIUS,
                    List.of(new Precondition("manual-investigation", false, "operator fallback"))));
        }
        return List.of();
    }

    static String computeHash(
            RootCause rootCause,
            ReconLoanFileState fcb,
            FacilityStatus novaStatus,
            List<OutboxRecordView> forwardOutboxRows,
            RemediationKind recommendedKind) {
        // Fold the per-uid peer SIGNAL (idempotency + dead-letter) into the maker-checker hash instead of a grace
        // boolean (LN-59513): a signal flip between dossier issue and operator approval changes the hash, so the
        // TOCTOU re-check in remediate() rejects a stale approval — strictly stronger than the old grace token.
        String payload = String.join(
                SEP,
                rootCause.name(),
                Boolean.toString(fcb.exists()),
                nullSafe(fcb.fileStatus()),
                novaStatus.name(),
                signalFingerprint(fcb, forwardOutboxRows),
                recommendedKind.name());

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static String signalFingerprint(ReconLoanFileState fcb, List<OutboxRecordView> forwardOutboxRows) {
        Map<String, EventPeerSignal> byUid = signalsByUid(fcb);
        List<String> tokens = new ArrayList<>();
        for (OutboxRecordView row : forwardOutboxRows) {
            String uid = eventIdOf(row);
            tokens.add(uid + ":" + statusNameOf(row) + ":" + signalToken(byUid.get(uid)));
        }
        tokens.sort(Comparator.naturalOrder());
        return String.join(",", tokens) + ";dltFacility=" + fcb.dltPresentForFacility();
    }

    private static String signalToken(@Nullable EventPeerSignal signal) {
        if (signal == null) {
            return "NONE";
        }
        return signal.idempotencyState().name() + "/" + (signal.dltDead() ? "DEAD" : "-") + "/"
                + nullSafe(signal.dltCategory());
    }

    private static boolean fcbSawForward(ReconLoanFileState fcb, List<OutboxRecordView> rows) {
        if (fcb.peerSignals().isEmpty()) {
            return false;
        }
        Map<String, EventPeerSignal> byUid = signalsByUid(fcb);
        for (OutboxRecordView row : rows) {
            EventPeerSignal signal = byUid.get(eventIdOf(row));
            if (signal != null
                    && (signal.idempotencyState() == EventPeerSignal.IdempotencyState.COMPLETED || signal.dltDead())) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, EventPeerSignal> signalsByUid(ReconLoanFileState fcb) {
        Map<String, EventPeerSignal> byUid = new HashMap<>();
        for (EventPeerSignal signal : fcb.peerSignals()) {
            if (signal.eventUid() != null) {
                byUid.put(signal.eventUid(), signal);
            }
        }
        return byUid;
    }

    private static String nullSafe(@Nullable String value) {
        return value == null ? "" : value;
    }

    private static String eventIdOf(OutboxRecordView row) {
        UUID eventId = row.eventId();
        return eventId == null ? "" : eventId.toString();
    }

    private static String statusNameOf(OutboxRecordView row) {
        MessageStatus status = row.status();
        return status == null ? "" : status.name();
    }

    private static long sequenceOf(OutboxRecordView row) {
        Long sequence = row.sequenceNumber();
        return sequence == null ? Long.MAX_VALUE : sequence;
    }
}
