package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.util.List;
import java.util.Objects;

import ir.dotin.platform.pangaea.reconciliation.api.model.Confidence;
import ir.dotin.platform.pangaea.reconciliation.api.model.RemediationKind;
import ir.dotin.platform.pangaea.reconciliation.api.model.RootCause;
import ir.dotin.platform.pangaea.reconciliation.api.model.SafetyTier;

record FacilityClassification(
        RootCause rootCause,
        Confidence confidence,
        SafetyTier safetyTier,
        RemediationKind recommendedKind,
        List<String> evidence) {

    FacilityClassification {
        Objects.requireNonNull(rootCause, "rootCause");
        Objects.requireNonNull(confidence, "confidence");
        Objects.requireNonNull(safetyTier, "safetyTier");
        Objects.requireNonNull(recommendedKind, "recommendedKind");
        evidence = List.copyOf(evidence);
    }
}
