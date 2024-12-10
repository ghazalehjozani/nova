package ir.dotin.loan.morabehe.core.application.service.config.saga;

import ir.dotin.loan.morabehe.core.application.service.config.route.LoanTypeRoutes;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanTypeUseCase;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.springframework.stereotype.Component;

@Component
public class CreateLoanTypeSagaConfigurator extends RouteBuilder {
    
    private final CreateLoanTypeUseCase createLoanTypeUseCase;

    public CreateLoanTypeSagaConfigurator(CreateLoanTypeUseCase createLoanTypeUseCase) {
        this.createLoanTypeUseCase = createLoanTypeUseCase;
    }

    @Override
    public void configure() throws Exception {
        // Exception Handling
        onException(Exception.class)
                .handled(true)
                .log("Exception in CreateLoanTypeUseCase: ${exception.message}")
                .bean("globalExceptionHandler", "process");

        // Main Saga Route for Creating Loan Type
        from(LoanTypeRoutes.CREATE_LOAN_TYPE_URI)
                .routeId(LoanTypeRoutes.SagaRoutes.CREATE_LOAN_TYPE_SAGA)
                .saga()
                .propagation(SagaPropagation.REQUIRED)
                .log("Starting saga for creating loan type")
                .log("Processing CreateLoanTypeCommand: ${body}")
                .bean(createLoanTypeUseCase, LoanTypeRoutes.EXECUTE_METHOD)
                .compensation(LoanTypeRoutes.COMPENSATE_CREATE_LOAN_TYPE_URI)
                .log("Loan type created with ID: ${body.loanTypeId}")
                .end();

        // Compensation Route for Creating Loan Type
        from(LoanTypeRoutes.COMPENSATE_CREATE_LOAN_TYPE_URI)
                .routeId(LoanTypeRoutes.SagaRoutes.CREATE_LOAN_TYPE_SAGA_COMPENSATION)
                .log("Compensating loan type creation")
                .bean(createLoanTypeUseCase, LoanTypeRoutes.COMPENSATE_METHOD)
                .end();

    }
}
