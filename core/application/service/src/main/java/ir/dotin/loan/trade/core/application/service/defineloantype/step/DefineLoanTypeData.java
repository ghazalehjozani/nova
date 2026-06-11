package ir.dotin.loan.trade.core.application.service.defineloantype.step;

import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineLoanTypeCommand;
import ir.dotin.loan.trade.core.application.service.defineloantype.component.LoanTypePrerequisitesLoader;

public record DefineLoanTypeData(DefineLoanTypeCommand command, LoanTypePrerequisitesLoader.Prerequisites prepared) {}
