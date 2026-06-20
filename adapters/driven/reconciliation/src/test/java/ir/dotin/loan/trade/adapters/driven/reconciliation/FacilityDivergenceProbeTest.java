package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.reconciliation.api.model.Divergence;
import ir.dotin.platform.pangaea.reconciliation.api.model.OpaqueKey;
import ir.dotin.platform.pangaea.reconciliation.api.model.Verdict;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconRow;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbReconStatePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconGuarantor;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconLoanFileState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Guarantor-drift detection in the divergence probe (LN-59442 M1). The status axis is held ALIGNED (non-terminal Nova,
 * FCB file at the expected rank) so only the guarantor overlay is under test. The detector must: flag drift only when
 * the flag is on and the sets genuinely differ; treat an absent/unknown side as not-a-drift (never false-positive); and
 * compare percentages by value (ignoring scale).
 */
@ExtendWith(MockitoExtension.class)
class FacilityDivergenceProbeTest {

    private static final String FACILITY = "11111111-1111-1111-1111-111111111111";

    @Mock
    private FacilityReconReadPort readPort;

    @Mock
    private FcbReconStatePort fcbReconStatePort;

    private ReconciliationSourceProperties properties;
    private FacilityDivergenceProbe probe;

    @BeforeEach
    void setUp() {
        properties = new ReconciliationSourceProperties();
        properties.setGuarantorDriftEnabled(true);
        probe = new FacilityDivergenceProbe(readPort, fcbReconStatePort, properties);
    }

    @Test
    void flagsGuarantorDriftWhenSetsDifferAndFlagOn() {
        stubNova(List.of(guarantor("111", 60), guarantor("222", 40)));
        stubFcb(List.of(fcbGuarantor("111", "60"))); // FCB missing guarantor 222 → keyset mismatch

        Divergence verdict = probe.probe(key());

        assertThat(verdict.verdict()).isEqualTo(Verdict.LAGGING);
        assertThat(verdict.reasonCode()).isEqualTo("guarantor-drift");
    }

    @Test
    void flagsGuarantorDriftWhenPercentageDiffers() {
        stubNova(List.of(guarantor("111", 60), guarantor("222", 40)));
        stubFcb(List.of(fcbGuarantor("111", "70"), fcbGuarantor("222", "30")));

        Divergence verdict = probe.probe(key());

        assertThat(verdict.verdict()).isEqualTo(Verdict.LAGGING);
        assertThat(verdict.reasonCode()).isEqualTo("guarantor-drift");
    }

    @Test
    void alignedWhenGuarantorSetsEqual() {
        stubNova(List.of(guarantor("111", 60), guarantor("222", 40)));
        stubFcb(List.of(fcbGuarantor("111", "60"), fcbGuarantor("222", "40")));

        Divergence verdict = probe.probe(key());

        assertThat(verdict.verdict()).isEqualTo(Verdict.ALIGNED);
    }

    @Test
    void percentScaleDifferenceIsNotDrift() {
        // FCB sends "100.0000", Nova "100" — equal by BigDecimal.compareTo, so NOT a drift.
        stubNova(List.of(guarantor("111", 100)));
        stubFcb(List.of(fcbGuarantor("111", "100.0000")));

        Divergence verdict = probe.probe(key());

        assertThat(verdict.verdict()).isEqualTo(Verdict.ALIGNED);
    }

    @Test
    void flagOffNeverFlagsDrift() {
        properties.setGuarantorDriftEnabled(false);
        stubNova(List.of(guarantor("111", 60)));
        stubFcb(List.of(fcbGuarantor("111", "40"))); // would be a drift if the flag were on

        Divergence verdict = probe.probe(key());

        assertThat(verdict.verdict()).isEqualTo(Verdict.ALIGNED);
        // Intent propagated: with the feature dark the probe asks the read port NOT to project guarantors, so the read
        // adapter skips the extra findGuarantorsById query (the perf gate).
        verify(readPort).findById(FACILITY, false);
        verify(readPort, never()).findById(FACILITY, true);
    }

    @Test
    void fcbGuarantorsAbsentIsNotDrift() {
        // FCB returned no guarantor data (older build / not gathered) → unknown side, never flag.
        stubNova(List.of(guarantor("111", 60)));
        stubFcb(List.of());

        Divergence verdict = probe.probe(key());

        assertThat(verdict.verdict()).isEqualTo(Verdict.ALIGNED);
    }

    @Test
    void fcbUnknownPercentageIsNotDrift() {
        stubNova(List.of(guarantor("111", 60)));
        stubFcb(List.of(new ReconGuarantor("111", null))); // percentage unknown → unknown side, never flag

        Divergence verdict = probe.probe(key());

        assertThat(verdict.verdict()).isEqualTo(Verdict.ALIGNED);
    }

    // ── helpers ──

    private void stubNova(List<ReconGuarantor> guarantors) {
        // The probe asks for guarantors only when the flag is on; stub both intents so the helper works either way.
        lenient()
                .when(readPort.findById(eq(FACILITY), anyBoolean()))
                .thenReturn(Optional.of(new FacilityReconRow(
                        FACILITY, FacilityStatus.APPROVED, 1_000_000L, "1-1404-10088-279", guarantors)));
    }

    private void stubFcb(List<ReconGuarantor> guarantors) {
        // APPROVE_LOAN = the expected FCB rank for Nova APPROVED → status axis ALIGNED, isolating the guarantor
        // overlay.
        ReconLoanFileState fcb = new ReconLoanFileState(
                true, "APPROVE_LOAN", "manual", 1_000_000L, true, null, List.of(), false, guarantors);
        when(fcbReconStatePort.loadReconState(eq(FACILITY), isNull())).thenReturn(Result.success(fcb));
    }

    private static ReconGuarantor guarantor(String customerNumber, int percent) {
        return new ReconGuarantor(customerNumber, BigDecimal.valueOf(percent));
    }

    private static ReconGuarantor fcbGuarantor(String customerNumber, String percent) {
        return new ReconGuarantor(customerNumber, new BigDecimal(percent));
    }

    private static OpaqueKey key() {
        return new OpaqueKey(FACILITY);
    }
}
