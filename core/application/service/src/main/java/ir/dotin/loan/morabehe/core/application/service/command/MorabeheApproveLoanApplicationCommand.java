package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.core.application.service.command.loanapplication.BaseApproveLoanApplicationCommand;

import java.io.Serializable;

public record MorabeheApproveLoanApplicationCommand(BaseApproveLoanApplicationCommand loanApplication)
        implements Serializable {
}
