package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.compensation.step;

import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateIrregularDisbursementCommand;

public record RevertIrregularDisbursementData(
        CompensateIrregularDisbursementCommand command, ReversalPreparation prepared) {}
