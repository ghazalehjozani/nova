package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.compensation.step;

import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateContractIssuanceCommand;

public record RevertContractIssuanceData(CompensateContractIssuanceCommand command, ReversalPreparation prepared) {}
