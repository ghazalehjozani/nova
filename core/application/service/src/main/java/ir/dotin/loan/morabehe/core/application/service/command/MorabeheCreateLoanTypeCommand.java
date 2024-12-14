package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.core.application.service.command.config.BaseCreateLoanTypeCommand;

public record MorabeheCreateLoanTypeCommand(Boolean hasIssueMerchandiseDocument,
                                            BaseCreateLoanTypeCommand loanType) {

}
