package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.compensation.step;

import java.util.concurrent.atomic.AtomicReference;

import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;

public record ReversalPreparation(AtomicReference<TrackedTransactionNumber> reversals) {}
