package ir.dotin.loan.morabehe.core.application.service.command;


public record UpdateLoanRuleCommand(CreateLoanRuleCommand oldLoanRuleCommand
        , CreateLoanRuleCommand newLoanRuleCommand) implements Command {

    @Override
    public String type() {
        return "UpdateLoanRuleCommand";
    }
}
