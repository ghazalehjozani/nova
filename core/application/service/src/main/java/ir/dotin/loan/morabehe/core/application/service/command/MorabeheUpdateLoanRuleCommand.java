package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.application.service.config.command.BaseCreateLoanRuleCommand;
import java.util.UUID;

public record MorabeheUpdateLoanRuleCommand(UUID loanRuleId, BaseCreateLoanRuleCommand loanRule) {

}
