package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.application.service.loanapplication.command.BaseCreateLoanApplicationCommand;
import java.util.UUID;

public record MorabeheCreateLoanApplicationCommand(
        UUID loanTypeId,
        UUID loanRuleId,
        BaseCreateLoanApplicationCommand loanApplication) {

}
