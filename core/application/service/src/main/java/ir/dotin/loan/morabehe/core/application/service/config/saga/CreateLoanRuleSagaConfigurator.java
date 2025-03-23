package ir.dotin.loan.morabehe.core.application.service.config.saga;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.springframework.stereotype.Component;

import ir.dotin.loan.baseloan.core.application.service.config.route.BaseRoutes;
import ir.dotin.loan.morabehe.core.application.service.config.route.LoanRuleRoutes;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanRuleUseCase;

@Component
public class CreateLoanRuleSagaConfigurator extends RouteBuilder {

    private final CreateLoanRuleUseCase createUseCase;

    public CreateLoanRuleSagaConfigurator(CreateLoanRuleUseCase createUseCase) {
        this.createUseCase = createUseCase;
    }

    @Override
    public void configure() throws Exception {
        // @formatter:off

        // Main Saga Route for Creating Loan Rule
        from(LoanRuleRoutes.CREATE_LOAN_RULE_URI)
                .routeId(LoanRuleRoutes.SagaRoutes.CREATE_LOAN_RULE_SAGA)
                .saga()
                .propagation(SagaPropagation.REQUIRED)
                .log("Starting saga for creating loan rule")
                .log(LoggingLevel.DEBUG, "Processing CreateLoanRuleCommand: ${body}")
                .bean(createUseCase, BaseRoutes.EXECUTE_METHOD)
                .compensation(LoanRuleRoutes.COMPENSATE_CREATE_LOAN_RULE_URI)
                .log(LoggingLevel.DEBUG, "Loan rule created with ID: ${body.serial}")
                .end();

        // Compensation Route for Creating Loan Rule
        from(LoanRuleRoutes.COMPENSATE_CREATE_LOAN_RULE_URI)
                .routeId(LoanRuleRoutes.SagaRoutes.CREATE_LOAN_RULE_SAGA_COMPENSATION)
                .log(LoggingLevel.DEBUG, "Compensating loan rule creation")
                .bean(createUseCase, BaseRoutes.COMPENSATE_METHOD)
                .end();

        // @formatter:on
    }
}
