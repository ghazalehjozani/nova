package ir.dotin.loan.morabehe.adapters.web.route;


import ir.dotin.loan.morabehe.adapters.web.exception.GlobalExceptionHandler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheApproveLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.handler.MorabeheApproveLoanApplicationHandler;
import ir.dotin.loan.morabehe.core.application.service.handler.MorabeheCreateLoanApplicationHandler;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class LoanApplicationRoute extends RouteBuilder {

    private final GlobalExceptionHandler globalExceptionHandler;

    public LoanApplicationRoute(GlobalExceptionHandler globalExceptionHandler) {
        this.globalExceptionHandler = globalExceptionHandler;
    }

    @Override
    public void configure() throws Exception {
        // @formatter:off
        onException(Exception.class)
                .handled(true)
                .process(globalExceptionHandler);

        rest("/v1/loan-application")
            .consumes(MediaType.APPLICATION_JSON_VALUE)
            .produces(MediaType.APPLICATION_JSON_VALUE)
                .post("/create")
                    .routeId("LoanApplication.create.post")
                    .type(MorabeheCreateLoanApplicationCommand.class)
                    .outType(LoanApplicationResponse.class)
                    .to("direct:createMorabeheLoanApplication")
                .post("/approve")
                .routeId("LoanApplication.approve.post")
                .type(MorabeheApproveLoanApplicationCommand.class)
                .outType(LoanApplicationResponse.class)
                .to("direct:approveMorabeheLoanApplication");

        from("direct:createMorabeheLoanApplication")
                .bean(MorabeheCreateLoanApplicationHandler.class, "handle");

        from("direct:approveMorabeheLoanApplication")
                .bean(MorabeheApproveLoanApplicationHandler.class, "handle");
        // @formatter:on
    }

}
