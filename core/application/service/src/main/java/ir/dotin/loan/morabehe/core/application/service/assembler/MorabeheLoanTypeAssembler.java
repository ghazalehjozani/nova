package ir.dotin.loan.morabehe.core.application.service.assembler;

import ir.dotin.loan.baseloan.application.service.assembler.config.BaseLoanTypeAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanTypeCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanTypeResponse;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.LoanType;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import org.springframework.stereotype.Component;


@Component
public class MorabeheLoanTypeAssembler extends
        BaseLoanTypeAssembler<LoanType.LoanTypeBuilder> {

    public MorabeheLoanType mapToAggregateRoot(MorabeheCreateLoanTypeCommand command) {
        LoanType.LoanTypeBuilder loanTypeBuilder = new LoanType.LoanTypeBuilder()
                .withHasIssueMerchandiseDocument(command.hasIssueMerchandiseDocument())
                .withLoanRuleIds(mapSet(command.loanType().loanRuleIds(), MorabeheLoanRuleId::new));
        super.mapFromCommand(command.loanType(), loanTypeBuilder);
        return new MorabeheLoanType(null, loanTypeBuilder);
    }

    public LoanTypeResponse mapToResponse(MorabeheLoanType loanType) {
        return new LoanTypeResponse(loanType.getId().value());
    }

}
