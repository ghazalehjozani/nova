package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.application.service.command.BaseCreateLoanRuleCommand;
import ir.dotin.loan.baseloan.application.service.command.Command;

import java.util.UUID;

public record MorabeheUpdateLoanRuleCommand(UUID loanRuleId, BaseCreateLoanRuleCommand loanRule) implements Command {

}
