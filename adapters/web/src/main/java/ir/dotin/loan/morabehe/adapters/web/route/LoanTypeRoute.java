package ir.dotin.loan.morabehe.adapters.web.route;


import ir.dotin.loan.morabehe.adapters.web.exception.GlobalExceptionHandler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanTypeCommand;
import ir.dotin.loan.morabehe.core.application.service.handler.MorabeheCreateLoanTypeHandler;
import ir.dotin.loan.morabehe.core.application.service.response.LoanTypeResponse;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class LoanTypeRoute extends RouteBuilder {

    private final GlobalExceptionHandler globalExceptionHandler;

    public LoanTypeRoute(GlobalExceptionHandler globalExceptionHandler) {
        this.globalExceptionHandler = globalExceptionHandler;
    }

    @Override
    public void configure() throws Exception {
        // @formatter:off
        onException(Exception.class)
                .handled(true)
                .process(globalExceptionHandler);

        rest("/v1/loan-type")
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .post("/create")
                .routeId("loanType.create.post")
                .type(MorabeheCreateLoanTypeCommand.class)
                .outType(LoanTypeResponse.class)
                .to("direct:createMorabeheLoanType");

        from("direct:createMorabeheLoanType")
                .bean(MorabeheCreateLoanTypeHandler.class, "handle");
        // @formatter:on
    }
}
