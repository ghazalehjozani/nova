package ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.step;

import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.component.ArrangementPreparation;

public record DefineArrangementData(DefineTradeLoanArrangementCommand command, ArrangementPreparation prepared) {}
