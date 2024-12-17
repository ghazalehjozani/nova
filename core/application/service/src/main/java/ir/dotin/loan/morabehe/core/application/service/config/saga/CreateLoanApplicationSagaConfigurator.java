package ir.dotin.loan.morabehe.core.application.service.config.saga;

import ir.dotin.loan.baseloan.core.application.service.config.route.BaseRoutes;
import ir.dotin.loan.morabehe.core.application.service.config.route.LoanApplicationRoutes;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanApplicationUseCase;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.springframework.stereotype.Component;

@Component
public class CreateLoanApplicationSagaConfigurator extends RouteBuilder {

    private final CreateLoanApplicationUseCase createUseCase;

    public CreateLoanApplicationSagaConfigurator(CreateLoanApplicationUseCase createUseCase) {
        this.createUseCase = createUseCase;
    }

    @Override
    public void configure() {
        // @formatter:off

        // Main Saga Route for Creating Loan Application
        from(LoanApplicationRoutes.CREATE_LOAN_APPLICATION_URI)
                .routeId(LoanApplicationRoutes.SagaRoutes.CREATE_LOAN_APPLICATION_SAGA)
                .saga()
                    .propagation(SagaPropagation.REQUIRED)
                    .log("Starting saga for creating loan application")
                    .log("Processing CreateLoanApplicationCommand: ${body}")
                    .bean(createUseCase, BaseRoutes.EXECUTE_METHOD)
                    .compensation(LoanApplicationRoutes.COMPENSATE_CREATE_LOAN_APPLICATION_URI)
                    .log("Loan application created with ID: ${body.loanApplicationId}")
                .end();

        // Compensation Route for Creating Loan Application
        from(LoanApplicationRoutes.COMPENSATE_CREATE_LOAN_APPLICATION_URI)
                .routeId(LoanApplicationRoutes.SagaRoutes.CREATE_LOAN_APPLICATION_SAGA_COMPENSATION)
                .log("Compensating loan application creation")
                .bean(createUseCase, BaseRoutes.COMPENSATE_METHOD)
                .end();

        // @formatter:on
    }

}
