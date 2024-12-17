package ir.dotin.loan.morabehe.core.application.service.config.saga;


import ir.dotin.loan.baseloan.core.application.service.config.route.BaseRoutes;
import ir.dotin.loan.morabehe.core.application.service.config.route.LoanRuleRoutes;
import ir.dotin.loan.morabehe.core.application.service.usecase.UpdateLoanRuleUseCase;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.springframework.stereotype.Component;

@Component
public class UpdateLoanRuleSagaConfigurator extends RouteBuilder {

    private final UpdateLoanRuleUseCase updateLoanRuleUseCase;

    public UpdateLoanRuleSagaConfigurator(UpdateLoanRuleUseCase updateLoanRuleUseCase) {
        this.updateLoanRuleUseCase = updateLoanRuleUseCase;
    }

    @Override
    public void configure() throws Exception {
        // @formatter:off

        // Main Saga Route for Updating loan Rule
        from(LoanRuleRoutes.UPDATE_LOAN_RULE_URI)
                .routeId(LoanRuleRoutes.SagaRoutes.UPDATE_LOAN_RULE_SAGA)
                .saga()
                    .propagation(SagaPropagation.REQUIRED)
                    .log("Starting saga for updating loan rule")
                    .log("Processing UpdateLoanRuleCommand: ${body}")
                    .bean(updateLoanRuleUseCase, BaseRoutes.EXECUTE_METHOD)
                    .compensation(LoanRuleRoutes.COMPENSATE_UPDATE_LOAN_RULE_URI)
                    .log("Loan rule updated with ID: ${body.loanRuleId}")
                .end();

        // Compensation Route for Updating Loan Rule
        from(LoanRuleRoutes.COMPENSATE_UPDATE_LOAN_RULE_URI)
                .routeId(LoanRuleRoutes.SagaRoutes.UPDATE_LOAN_RULE_SAGA_COMPENSATION)
                .log("Compensating loan rule update")
                .bean(updateLoanRuleUseCase, BaseRoutes.COMPENSATE_METHOD)
                .end();

        // @formatter:on
    }
}
