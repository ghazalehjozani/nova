package ir.dotin.loan.morabehe.core.application.service.mapper;

import ir.dotin.loan.baseloan.application.service.mapper.config.BaseLoanTypeCommandMapper;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleId;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanTypeCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanTypeResponse;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.LoanType;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import org.springframework.stereotype.Component;


@Component
public class MorabeheLoanTypeCommandMapper extends
        BaseLoanTypeCommandMapper<LoanType.LoanTypeBuilder> {

    public MorabeheLoanType mapToAggregateRoot(MorabeheCreateLoanTypeCommand command) {
        LoanType.LoanTypeBuilder loanTypeBuilder = new LoanType.LoanTypeBuilder()
                .withLoanRuleIds(mapSet(command.loanRuleIds(), LoanRuleId::new));
        loanTypeBuilder.withHasIssueMerchandiseDocument(command.hasIssueMerchandiseDocument())
                .withLoanRuleIds(mapSet(command.loanRuleIds(), LoanRuleId::new));
        super.mapFromCommand(command.loanType(), loanTypeBuilder);
        return new MorabeheLoanType(null, loanTypeBuilder);
    }

    public LoanTypeResponse mapToResponse(MorabeheLoanType loanType) {
        return new LoanTypeResponse(loanType.getLoanType().getId().value());
    }

}
