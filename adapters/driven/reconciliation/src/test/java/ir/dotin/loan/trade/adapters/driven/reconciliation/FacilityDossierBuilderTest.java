package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.Test;

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

import static org.assertj.core.api.Assertions.assertThat;

class FacilityDossierBuilderTest {

    private final FacilityDossierBuilder builder = new FacilityDossierBuilder();

    @Test
    void buildsReplayForwardDossierForApplyLostMoney() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        OutboxRecordView row1 = forward(id1, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L);
        OutboxRecordView row2 = forward(id2, "TRADE_LOAN_FACILITY_DISBURSED", MessageStatus.PROCESSED, 20L);
        List<OutboxRecordView> rows = List.of(row2, row1);

        FacilityClassification classification = new FacilityClassification(
                RootCause.FCB_APPLY_LOST,
                Confidence.HIGH,
                SafetyTier.MONEY_GATED,
                RemediationKind.REPLAY_FORWARD,
                List.of("fcb.exists=false", "money=true"));

        OperatorDossier dossier = builder.build(
                FacilityStatus.FULLY_DISBURSED, fcbAbsent(), rows, "DISBURSED", true, classification, null);

        assertThat(dossier.rootCause()).isEqualTo(RootCause.FCB_APPLY_LOST);
        assertThat(dossier.confidence()).isEqualTo(Confidence.HIGH);
        assertThat(dossier.graceElapsed()).isTrue();

        ProposedRemediation recommended = dossier.recommended();
        assertThat(recommended.kind()).isEqualTo(RemediationKind.REPLAY_FORWARD);
        assertThat(recommended.safetyTier()).isEqualTo(SafetyTier.MONEY_GATED);
        assertThat(recommended.idempotent()).isTrue();
        assertThat(recommended.idempotencyKeys()).containsExactly(id1.toString(), id2.toString());
        assertThat(recommended.expectedEffect()).contains("2");
        assertThat(recommended.reversible()).isFalse();
        assertThat(recommended.preconditions())
                .extracting(Precondition::satisfied)
                .containsOnly(true);

        NovaSnapshot nova = Objects.requireNonNull(dossier.novaSnapshot());
        assertThat(nova.status()).isEqualTo("FULLY_DISBURSED");
        assertThat(nova.workflowState()).isEqualTo("DISBURSED");
        assertThat(nova.lastForwardEvents())
                .containsExactly("TRADE_LOAN_FACILITY_FULLY_DISBURSED", "TRADE_LOAN_FACILITY_DISBURSED");
        assertThat(nova.outboxRows()).hasSize(2);
        assertThat(nova.outboxRows()).extracting(OutboxRowSummary::sent).containsOnly(true);
        assertThat(nova.outboxRows()).extracting(OutboxRowSummary::consumed).containsOnly(false);

        FcbSnapshot fcb = Objects.requireNonNull(dossier.fcbSnapshot());
        assertThat(fcb.exists()).isFalse();
        assertThat(fcb.reachable()).isTrue();

        assertThat(dossier.alternatives())
                .extracting(ProposedRemediation::kind)
                .containsExactly(RemediationKind.MANUAL_DATA_FIX);

        assertThat(dossier.dossierHash()).isNotBlank();
    }

    @Test
    void buildsReverseNovaDossierForPhantom() {
        FacilityClassification classification = new FacilityClassification(
                RootCause.NOVA_PHANTOM,
                Confidence.HIGH,
                SafetyTier.MANUAL_ONLY,
                RemediationKind.REVERSE_NOVA,
                List.of("fcb.exists=false"));

        OperatorDossier dossier =
                builder.build(FacilityStatus.APPROVED, fcbAbsent(), List.of(), null, true, classification, null);

        assertThat(dossier.recommended().kind()).isEqualTo(RemediationKind.REVERSE_NOVA);
        assertThat(dossier.recommended().safetyTier()).isEqualTo(SafetyTier.MANUAL_ONLY);
        assertThat(dossier.recommended().idempotent()).isFalse();
        assertThat(dossier.recommended().idempotencyKeys()).isEmpty();
        assertThat(dossier.alternatives())
                .extracting(ProposedRemediation::kind)
                .containsExactly(RemediationKind.MANUAL_DATA_FIX);
    }

    @Test
    void buildsNoneRemediationLeavesNoAlternatives() {
        FacilityClassification classification = new FacilityClassification(
                RootCause.FCB_LAG, Confidence.HIGH, SafetyTier.AUTO_SAFE, RemediationKind.NONE, List.of());

        OperatorDossier dossier = builder.build(
                FacilityStatus.APPROVED,
                fcbAbsent(),
                List.of(forward(UUID.randomUUID(), "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED, 1L)),
                null,
                false,
                classification,
                null);

        assertThat(dossier.recommended().kind()).isEqualTo(RemediationKind.NONE);
        assertThat(dossier.alternatives()).isEmpty();
    }

    @Test
    void hashIsDeterministicForSameDecisionInputs() {
        List<OutboxRecordView> rows = List.of(
                forward(UUID.fromString("00000000-0000-0000-0000-000000000001"), "E1", MessageStatus.PROCESSED, 1L),
                forward(UUID.fromString("00000000-0000-0000-0000-000000000002"), "E2", MessageStatus.PROCESSED, 2L));

        String a = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST,
                fcbAbsent(),
                FacilityStatus.APPROVED,
                rows,
                true,
                RemediationKind.REPLAY_FORWARD);
        String b = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST,
                fcbAbsent(),
                FacilityStatus.APPROVED,
                rows,
                true,
                RemediationKind.REPLAY_FORWARD);

        assertThat(a).isEqualTo(b);
        assertThat(a).matches("[0-9a-f]{64}");
    }

    @Test
    void hashIgnoresCosmeticFcbFields() {
        List<OutboxRecordView> rows = List.of(forward(UUID.randomUUID(), "E1", MessageStatus.PROCESSED, 1L));

        ReconLoanFileState base = new ReconLoanFileState(false, "REQUEST_LOAN", "manual-A", 100L, true, "outbox-A");
        ReconLoanFileState cosmeticlyChanged =
                new ReconLoanFileState(false, "REQUEST_LOAN", "manual-Z", 999999L, true, "outbox-Z");

        String a = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST, base, FacilityStatus.APPROVED, rows, true, RemediationKind.REPLAY_FORWARD);
        String b = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST,
                cosmeticlyChanged,
                FacilityStatus.APPROVED,
                rows,
                true,
                RemediationKind.REPLAY_FORWARD);

        assertThat(a).isEqualTo(b);
    }

    @Test
    void hashChangesWhenFcbExistsFlips() {
        List<OutboxRecordView> rows = List.of(forward(UUID.randomUUID(), "E1", MessageStatus.PROCESSED, 1L));
        ReconLoanFileState absent = new ReconLoanFileState(false, "REQUEST_LOAN", "manual", 1L, true, null);
        ReconLoanFileState present = new ReconLoanFileState(true, "REQUEST_LOAN", "manual", 1L, true, null);

        String a = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST, absent, FacilityStatus.APPROVED, rows, true, RemediationKind.REPLAY_FORWARD);
        String b = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST, present, FacilityStatus.APPROVED, rows, true, RemediationKind.REPLAY_FORWARD);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void hashChangesWhenForwardRowStatusChanges() {
        UUID id = UUID.randomUUID();
        List<OutboxRecordView> processed = List.of(forward(id, "E1", MessageStatus.PROCESSED, 1L));
        List<OutboxRecordView> dead = List.of(forward(id, "E1", MessageStatus.DEAD_LETTER, 1L));

        String a = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST,
                fcbAbsent(),
                FacilityStatus.APPROVED,
                processed,
                true,
                RemediationKind.REPLAY_FORWARD);
        String b = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST,
                fcbAbsent(),
                FacilityStatus.APPROVED,
                dead,
                true,
                RemediationKind.REPLAY_FORWARD);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void hashChangesWhenNovaStatusChanges() {
        List<OutboxRecordView> rows = List.of(forward(UUID.randomUUID(), "E1", MessageStatus.PROCESSED, 1L));

        String a = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST,
                fcbAbsent(),
                FacilityStatus.APPROVED,
                rows,
                true,
                RemediationKind.REPLAY_FORWARD);
        String b = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST,
                fcbAbsent(),
                FacilityStatus.FULLY_DISBURSED,
                rows,
                true,
                RemediationKind.REPLAY_FORWARD);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void hashIsOrderIndependentForForwardRows() {
        OutboxRecordView r1 =
                forward(UUID.fromString("00000000-0000-0000-0000-000000000001"), "E1", MessageStatus.PROCESSED, 1L);
        OutboxRecordView r2 =
                forward(UUID.fromString("00000000-0000-0000-0000-000000000002"), "E2", MessageStatus.PROCESSED, 2L);

        String a = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST,
                fcbAbsent(),
                FacilityStatus.APPROVED,
                List.of(r1, r2),
                true,
                RemediationKind.REPLAY_FORWARD);
        String b = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST,
                fcbAbsent(),
                FacilityStatus.APPROVED,
                List.of(r2, r1),
                true,
                RemediationKind.REPLAY_FORWARD);

        assertThat(a).isEqualTo(b);
    }

    @Test
    void hashChangesWhenRecommendedKindChanges() {
        List<OutboxRecordView> rows = List.of(forward(UUID.randomUUID(), "E1", MessageStatus.PROCESSED, 1L));

        String a = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST,
                fcbAbsent(),
                FacilityStatus.APPROVED,
                rows,
                true,
                RemediationKind.REPLAY_FORWARD);
        String b = FacilityDossierBuilder.computeHash(
                RootCause.FCB_APPLY_LOST,
                fcbAbsent(),
                FacilityStatus.APPROVED,
                rows,
                true,
                RemediationKind.MANUAL_DATA_FIX);

        assertThat(a).isNotEqualTo(b);
    }

    private static ReconLoanFileState fcbAbsent() {
        return new ReconLoanFileState(false, null, "manual", 1L, true, null);
    }

    private static OutboxRecordView forward(UUID eventId, String eventType, MessageStatus status, long sequence) {
        return new OutboxRecordView(
                UUID.randomUUID(),
                eventId,
                "idem-" + eventType,
                UUID.randomUUID(),
                "LoanFacility",
                eventType,
                1,
                sequence,
                "trade-loan.facility.events",
                "partition-key",
                status,
                0,
                (Instant) null,
                (String) null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                (Instant) null,
                (Instant) null,
                (String) null);
    }
}
