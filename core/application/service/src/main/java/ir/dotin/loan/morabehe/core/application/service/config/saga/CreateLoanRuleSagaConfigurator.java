package ir.dotin.loan.morabehe.core.application.service.config.saga;

import ir.dotin.loan.morabehe.core.application.service.config.route.LoanRuleRoutes;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanRuleUseCase;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.springframework.stereotype.Component;


@Component
public class CreateLoanRuleSagaConfigurator extends RouteBuilder {


    private final CreateLoanRuleUseCase createUseCase;

    public CreateLoanRuleSagaConfigurator(CreateLoanRuleUseCase createUseCase) {
        this.createUseCase = createUseCase;
    }

    @Override
    public void configure() throws Exception {

        // Exception Handling
        onException(Exception.class)
                .handled(true)
                .log("Exception in CreateLoanRuleUseCase: ${exception.message}")
                .bean("globalExceptionHandler", "process");

        // Main Saga Route for Creating Loan Rule
        from(LoanRuleRoutes.CREATE_LOAN_RULE_URI)
                .routeId(LoanRuleRoutes.SagaRoutes.CREATE_LOAN_RULE_SAGA)
                .saga()
                .propagation(SagaPropagation.REQUIRED)
                .log("Starting saga for creating loan rule")
                .log("Processing CreateLoanRuleCommand: ${body}")
                .bean(createUseCase, LoanRuleRoutes.EXECUTE_METHOD)
                .compensation(LoanRuleRoutes.COMPENSATE_CREATE_LOAN_RULE_URI)
                .log("Loan rule created with ID: ${body.loanRuleId}")
                .end();

        // Compensation Route for Creating Loan Rule
        from(LoanRuleRoutes.COMPENSATE_CREATE_LOAN_RULE_URI)
                .routeId(LoanRuleRoutes.SagaRoutes.CREATE_LOAN_RULE_SAGA_COMPENSATION)
                .log("Compensating loan rule creation")
                .bean(createUseCase, LoanRuleRoutes.COMPENSATE_METHOD)
                .end();

    }
}
