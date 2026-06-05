package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconLoanFileState;

final class FacilityDossierBuilder {

    private static final String SEP = "|";
    private static final String BLAST_RADIUS = "single facility";

    OperatorDossier build(
            FacilityStatus novaStatus,
            ReconLoanFileState fcb,
            List<OutboxRecordView> forwardOutboxRows,
            @Nullable String sagaState,
            boolean graceElapsed,
            FacilityClassification classification,
            @Nullable String fcbBusinessError) {
        Objects.requireNonNull(novaStatus, "novaStatus");
        Objects.requireNonNull(fcb, "fcb");
        Objects.requireNonNull(classification, "classification");
        List<OutboxRecordView> rows = List.copyOf(forwardOutboxRows);

        NovaSnapshot novaSnapshot = novaSnapshot(novaStatus, rows, sagaState);
        FcbSnapshot fcbSnapshot = new FcbSnapshot(fcb.reachable(), fcb.exists(), fcb.fileStatus(), fcbBusinessError);

        ProposedRemediation recommended = recommendation(classification, fcb, rows, graceElapsed);
        List<ProposedRemediation> alternatives = alternatives(recommended.kind());

        String hash = computeHash(classification.rootCause(), fcb, novaStatus, rows, graceElapsed, recommended.kind());

        return new OperatorDossier(
                classification.rootCause(),
                classification.confidence(),
                novaSnapshot,
                fcbSnapshot,
                graceElapsed,
                classification.evidence(),
                recommended,
                alternatives,
                hash);
    }

    private NovaSnapshot novaSnapshot(
            FacilityStatus novaStatus, List<OutboxRecordView> rows, @Nullable String sagaState) {
        List<String> lastForwardEvents = rows.stream()
                .sorted(Comparator.comparing(FacilityDossierBuilder::sequenceOf))
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
        return new NovaSnapshot(novaStatus.name(), lastForwardEvents, outboxRows, sagaState);
    }

    private ProposedRemediation recommendation(
            FacilityClassification classification,
            ReconLoanFileState fcb,
            List<OutboxRecordView> rows,
            boolean graceElapsed) {
        return switch (classification.recommendedKind()) {
            case REPLAY_FORWARD -> replayForward(classification, fcb, rows, graceElapsed);
            case REVERSE_NOVA -> reverseNova(classification, fcb);
            case MANUAL_DATA_FIX -> manualDataFix(classification, fcb);
            case NONE -> ProposedRemediation.none();
        };
    }

    private ProposedRemediation replayForward(
            FacilityClassification classification,
            ReconLoanFileState fcb,
            List<OutboxRecordView> rows,
            boolean graceElapsed) {
        List<String> keys = rows.stream()
                .filter(row -> row.status() == MessageStatus.PROCESSED)
                .sorted(Comparator.comparing(FacilityDossierBuilder::sequenceOf))
                .map(FacilityDossierBuilder::eventIdOf)
                .toList();
        int n = keys.size();
        List<Precondition> preconditions = List.of(
                new Precondition("grace-elapsed", graceElapsed, "graceElapsed=" + graceElapsed),
                new Precondition("fcb-absent", !fcb.exists(), "fcb.exists=" + fcb.exists()),
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

    private ProposedRemediation manualDataFix(FacilityClassification classification, ReconLoanFileState fcb) {
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
            boolean graceElapsed,
            RemediationKind recommendedKind) {
        List<String> rowTokens = new ArrayList<>();
        for (OutboxRecordView row : forwardOutboxRows) {
            rowTokens.add(eventIdOf(row) + ":" + statusNameOf(row));
        }
        rowTokens.sort(Comparator.naturalOrder());

        String payload = String.join(
                SEP,
                rootCause.name(),
                Boolean.toString(fcb.exists()),
                nullSafe(fcb.fileStatus()),
                novaStatus.name(),
                String.join(",", rowTokens),
                Boolean.toString(graceElapsed),
                recommendedKind.name());

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
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
