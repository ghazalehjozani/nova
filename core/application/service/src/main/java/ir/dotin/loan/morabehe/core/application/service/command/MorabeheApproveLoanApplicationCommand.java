package ir.dotin.loan.morabehe.core.application.service.command;

import java.io.Serializable;

import ir.dotin.loan.baseloan.core.application.service.command.loanapplication.BaseApproveLoanApplicationCommand;

public record MorabeheApproveLoanApplicationCommand(BaseApproveLoanApplicationCommand loanApplication)
        implements Serializable {}
