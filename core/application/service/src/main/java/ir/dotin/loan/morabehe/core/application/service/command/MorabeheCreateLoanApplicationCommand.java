package ir.dotin.loan.morabehe.core.application.service.command;

import java.io.Serializable;

import ir.dotin.loan.baseloan.core.application.service.command.loanapplication.BaseCreateLoanApplicationCommand;

public record MorabeheCreateLoanApplicationCommand(BaseCreateLoanApplicationCommand loanApplication)
        implements Serializable {}
