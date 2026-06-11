package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.compensation.step;

import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateLumpSumDisbursementCommand;

public record RevertLumpSumData(CompensateLumpSumDisbursementCommand command, ReversalPreparation prepared) {}
