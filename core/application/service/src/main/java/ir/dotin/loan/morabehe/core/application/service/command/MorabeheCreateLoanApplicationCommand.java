package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.core.application.service.command.loanapplication.BaseCreateLoanApplicationCommand;

import java.io.Serializable;

public record MorabeheCreateLoanApplicationCommand(
        BaseCreateLoanApplicationCommand loanApplication) implements Serializable {

}
