package ir.dotin.loan.morabehe.core.application.service.config.saga;

import ir.dotin.loan.baseloan.core.application.service.config.route.BaseRoutes;
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
        // @formatter:off

        // Main Saga Route for Approving Loan Application
        from(LoanApplicationRoutes.APPROVE_LOAN_APPLICATION_URI)
                .errorHandler(noErrorHandler())
                .routeId(LoanApplicationRoutes.SagaRoutes.APPROVE_LOAN_APPLICATION_SAGA)
                .saga()
                    .propagation(SagaPropagation.REQUIRED)
                    .log("Starting saga for approving loan application at DEBUG level")
                    .log("Processing ApproveLoanApplicationCommand: ${body}")
                    .bean(approveUseCase, BaseRoutes.EXECUTE_METHOD)
                    .compensation(LoanApplicationRoutes.COMPENSATE_APPROVE_LOAN_APPLICATION_URI)
                    .log("Loan application approved with ID: ${body.loanApplicationId} at DEBUG level")
                .end();

        // Compensation Route for Approving Loan Application
        from(LoanApplicationRoutes.COMPENSATE_APPROVE_LOAN_APPLICATION_URI)
                .routeId(LoanApplicationRoutes.SagaRoutes.APPROVE_LOAN_APPLICATION_SAGA_COMPENSATION)
                .log("Compensating loan application approval at DEBUG level")
                .bean(approveUseCase, BaseRoutes.COMPENSATE_METHOD)
                .end();

        // @formatter:on
    }

}
