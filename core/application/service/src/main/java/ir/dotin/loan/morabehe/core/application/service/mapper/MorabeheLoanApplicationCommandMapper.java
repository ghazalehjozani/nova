package ir.dotin.loan.morabehe.core.application.service.mapper;

import ir.dotin.loan.baseloan.application.service.mapper.loanapplication.BaseLoanApplicationCommandMapper;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleId;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanTypeId;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication.LoanApplicationBuilder;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MorabeheLoanApplicationCommandMapper extends
        BaseLoanApplicationCommandMapper<LoanApplicationBuilder> {


    public MorabeheLoanApplication mapToAggregateRoot(
            MorabeheCreateLoanApplicationCommand command) {
        var builder = new LoanApplicationBuilder(new FeatureConfig(Map.of("Key1", false)))
                .withLoanRuleId(mapOptional(command.loanRuleId(),
                                            id -> new LoanRuleId(UUID.fromString(id))))
                .withLoanTypeId(mapOptional(command.loanTypeId(),
                                            id -> new LoanTypeId(UUID.fromString(id))));
        super.mapFromCommand(command.loanApplication(), builder);
        return new MorabeheLoanApplication(null, builder.validateAndBuild());
    }

    public LoanApplicationResponse mapToResponse(MorabeheLoanApplication loanApplication) {
        return new LoanApplicationResponse(loanApplication.getId().value());
    }

}
