package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.compensation.step;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;

public record ReversalPreparation(AtomicReference<List<TrackedTransactionNumber>> reversals) {}
