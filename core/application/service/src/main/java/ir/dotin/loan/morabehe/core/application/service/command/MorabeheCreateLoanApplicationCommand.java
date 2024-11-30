package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.application.service.loanapplication.command.BaseCreateLoanApplicationCommand;
import java.io.Serializable;

public record MorabeheCreateLoanApplicationCommand(
        String loanTypeId,
        String loanRuleId,
        BaseCreateLoanApplicationCommand loanApplication) implements Serializable {

}
