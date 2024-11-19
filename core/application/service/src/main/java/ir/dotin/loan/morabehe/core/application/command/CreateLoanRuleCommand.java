package ir.dotin.loan.morabehe.core.application.command;

import ir.dotin.platform.ddd.application.common.command.Command;

public record CreateLoanRuleCommand(String type) implements Command {

    //TODO: Add LoanRule Fields

    @Override
    public String type() {
        return "CreateLoanRuleCommand";
    }
}
