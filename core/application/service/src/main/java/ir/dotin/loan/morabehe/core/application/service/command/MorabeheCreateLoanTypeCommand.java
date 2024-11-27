package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.application.service.command.BaseCreateLoanTypeCommand;
import ir.dotin.loan.baseloan.application.service.command.Command;

public record MorabeheCreateLoanTypeCommand(BaseCreateLoanTypeCommand loanType) implements Command {

}
