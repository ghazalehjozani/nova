package ir.dotin.loan.morabehe.core.application.service.mapper;

import ir.dotin.loan.baseloan.application.service.config.mapper.BaseLoanTypeCommandMapper;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanTypeCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanTypeResponse;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.LoanType;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import org.springframework.stereotype.Component;


@Component
public class MorabeheLoanTypeCommandMapper extends BaseLoanTypeCommandMapper<LoanType.LoanTypeBuilder> {

    public MorabeheLoanType mapToAggregateRoot(MorabeheCreateLoanTypeCommand command) {
        LoanType.LoanTypeBuilder loanTypeBuilder = new LoanType.LoanTypeBuilder();
        loanTypeBuilder.withHasIssueMerchandiseDocument(command.hasIssueMerchandiseDocument());
        super.mapCommonFields(command.loanType(), loanTypeBuilder);
        return new MorabeheLoanType(null, loanTypeBuilder);
    }

    public LoanTypeResponse mapToResponse(MorabeheLoanType loanType) {
        return new LoanTypeResponse(loanType.getLoanType().getId().value());
    }

}
