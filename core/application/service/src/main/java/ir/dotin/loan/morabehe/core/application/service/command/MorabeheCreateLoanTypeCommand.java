package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.application.service.config.command.BaseCreateLoanTypeCommand;

public record MorabeheCreateLoanTypeCommand(Boolean hasIssueMerchandiseDocument,
                                            BaseCreateLoanTypeCommand loanType) {

}
