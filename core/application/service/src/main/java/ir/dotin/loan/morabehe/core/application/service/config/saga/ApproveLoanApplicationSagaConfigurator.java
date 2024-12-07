package ir.dotin.loan.morabehe.core.application.service.config.saga;

import ir.dotin.loan.morabehe.core.application.service.config.route.LoanApplicationRoutes;
import ir.dotin.loan.morabehe.core.application.service.usecase.ApproveLoanApplicationUseCase;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.springframework.stereotype.Component;

@Component
public class ApproveLoanApplicationSagaConfigurator extends RouteBuilder {

    private final ApproveLoanApplicationUseCase approveUseCase;

    public ApproveLoanApplicationSagaConfigurator(ApproveLoanApplicationUseCase approveUseCase) {
        this.approveUseCase = approveUseCase;
    }

    @Override
    public void configure() {
        // Exception Handling
        onException(Exception.class)
                .handled(true)
                .log("Exception in ApproveLoanApplicationUseCase: ${exception.message}")
                .bean("globalExceptionHandler", "process");

        // Main Saga Route for Approving Loan Application
        from(LoanApplicationRoutes.APPROVE_LOAN_APPLICATION_URI)
                .routeId(LoanApplicationRoutes.SagaRoutes.APPROVE_LOAN_APPLICATION_SAGA)
                .saga()
                .propagation(SagaPropagation.REQUIRED)
                .log("Starting saga for approving loan application at DEBUG level")
                .log("Processing ApproveLoanApplicationCommand: ${body}")
                .bean(approveUseCase, LoanApplicationRoutes.EXECUTE_METHOD)
                .compensation(LoanApplicationRoutes.COMPENSATE_APPROVE_LOAN_APPLICATION_URI)
                .log("Loan application approved with ID: ${body.loanApplicationId} at DEBUG level")
                .end();

        // Compensation Route for Approving Loan Application
        from(LoanApplicationRoutes.COMPENSATE_APPROVE_LOAN_APPLICATION_URI)
                .routeId(LoanApplicationRoutes.SagaRoutes.APPROVE_LOAN_APPLICATION_SAGA_COMPENSATION)
                .log("Compensating loan application approval at DEBUG level")
                .bean(approveUseCase, LoanApplicationRoutes.COMPENSATE_METHOD)
                .end();
    }

}
