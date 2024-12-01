package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.application.service.command.loanapplication.BaseCreateLoanApplicationCommand;

import java.io.Serializable;
import java.math.BigDecimal;

public record MorabeheCreateLoanApplicationCommand(
        BaseCreateLoanApplicationCommand loanApplication,
        BigDecimal prePaymentAmount,
        String prePaymentDepositNumber) implements Serializable {

}
