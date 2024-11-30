package ir.dotin.loan.morabehe.core.application.service.command;

import ir.dotin.loan.baseloan.application.service.config.command.BaseCreateLoanTypeCommand;
import java.util.Set;
import java.util.UUID;

public record MorabeheCreateLoanTypeCommand(Boolean hasIssueMerchandiseDocument,
                                            Set<UUID> loanRuleIds,
                                            BaseCreateLoanTypeCommand loanType) {

}
