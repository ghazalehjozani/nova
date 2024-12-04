package ir.dotin.loan.morabehe.core.application.service.aggregateassembler;

import ir.dotin.loan.baseloan.application.service.aggregateassembler.loanapplication.BaseLoanApplicationAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheApproveLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication.LoanApplicationBuilder;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class MorabeheLoanApplicationAssembler extends
        BaseLoanApplicationAssembler<LoanApplicationBuilder> {


    public MorabeheLoanApplication mapToAggregateRoot(
            MorabeheCreateLoanApplicationCommand command) {
        var builder = new LoanApplicationBuilder(new FeatureConfig(Map.of("Key1", false)))
                .withLoanRuleId(mapOptional(command.loanRuleId(),
                                            id -> new MorabeheLoanRuleId(UUID.fromString(id))))
                .withLoanTypeId(mapOptional(command.loanTypeId(),
                                            id -> new MorabeheLoanTypeId(UUID.fromString(id))))
                .withCurrentState(command.loanApplication().getState());
        super.mapFromCommand(command.loanApplication(), builder);
        return new MorabeheLoanApplication(null, builder.validateAndBuild());
    }

    public MorabeheLoanApplication mapToAggregateRoot(
            MorabeheApproveLoanApplicationCommand command, MorabeheLoanApplication loanApplication) {
        var builder = new LoanApplication.LoanApplicationBuilder(new FeatureConfig(Map.of("Key1", false)),
                loanApplication.getLoanApplication());
        super.mapFromCommand(command.loanApplication(), builder);
        return new MorabeheLoanApplication(null, builder.validateAndBuild());
    }

    public LoanApplicationResponse mapToResponse(MorabeheLoanApplication loanApplication) {
        return new LoanApplicationResponse(loanApplication.getId().value());
    }

}
