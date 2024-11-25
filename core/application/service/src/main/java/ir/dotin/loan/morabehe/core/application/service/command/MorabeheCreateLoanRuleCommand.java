package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.application.service.command.BaseCreateLoanRuleCommand;
import ir.dotin.loan.baseloan.application.service.command.Command;

public record MorabeheCreateLoanRuleCommand(BaseCreateLoanRuleCommand loanRule) implements Command {

}
