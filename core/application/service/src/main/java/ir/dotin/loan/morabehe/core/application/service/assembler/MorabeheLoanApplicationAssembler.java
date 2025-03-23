package ir.dotin.loan.morabehe.core.application.service.assembler;

import java.util.Map;

import org.springframework.stereotype.Component;

import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import ir.dotin.loan.baseloan.core.application.service.assembler.loanapplication.BaseLoanApplicationAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheApproveLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication.LoanApplicationBuilder;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;

@Component
public class MorabeheLoanApplicationAssembler extends BaseLoanApplicationAssembler<LoanApplicationBuilder> {

    public MorabeheLoanApplication mapToAggregateRoot(MorabeheCreateLoanApplicationCommand command) {
        var builder = new LoanApplicationBuilder(new FeatureConfig(Map.of("Key1", false)))
                .withLoanRuleId(mapOptional(command.loanApplication().loanRuleId(), MorabeheLoanRuleId::new))
                .withLoanTypeId(mapOptional(command.loanApplication().loanTypeId(), MorabeheLoanTypeId::new))
                .withCurrentState(command.loanApplication().getState());
        super.mapFromCommand(command.loanApplication(), builder);
        return new MorabeheLoanApplication(null, builder.validateAndBuild());
    }

    public MorabeheLoanApplication mapToAggregateRoot(
            MorabeheApproveLoanApplicationCommand command, MorabeheLoanApplication loanApplication) {
        var builder = new LoanApplication.LoanApplicationBuilder(
                new FeatureConfig(Map.of("Key1", false)), loanApplication.loanApplication());
        super.mapFromCommand(command.loanApplication(), builder);
        return new MorabeheLoanApplication(null, builder.validateAndBuild());
    }

    public LoanApplicationResponse mapToResponse(MorabeheLoanApplication loanApplication) {
        return new LoanApplicationResponse(loanApplication.id().value());
    }
}
