package ir.dotin.loan.morabehe.adapters.web.route;


import ir.dotin.loan.morabehe.adapters.web.exception.GlobalExceptionHandler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheUpdateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.handler.MorabeheCreateLoanRuleHandler;
import ir.dotin.loan.morabehe.core.application.service.handler.MorabeheUpdateLoanRuleHandler;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class LoanRuleRoute extends RouteBuilder {

    private final GlobalExceptionHandler globalExceptionHandler;

    public LoanRuleRoute(GlobalExceptionHandler globalExceptionHandler) {
        this.globalExceptionHandler = globalExceptionHandler;
    }

    @Override
    public void configure() throws Exception {
        // @formatter:off
        onException(Exception.class)
                .handled(true)
                .process(globalExceptionHandler);

        rest("/v1/loan-rule")
            .consumes(MediaType.APPLICATION_JSON_VALUE)
            .produces(MediaType.APPLICATION_JSON_VALUE)
                .post("/create")
                    .routeId("loanRule.create.post")
                    .type(MorabeheCreateLoanRuleCommand.class)
                    .outType(LoanRuleResponse.class)
                    .to("direct:createMorabeheLoanRule")
                .put("/update")
                    .routeId("loanRule.update.put")
                    .type(MorabeheUpdateLoanRuleCommand.class)
                    .outType(LoanRuleResponse.class)
                    .to("direct:updateMorabeheLoanRule");

        from("direct:createMorabeheLoanRule")
                .bean(MorabeheCreateLoanRuleHandler.class, "handle");

        from("direct:updateMorabeheLoanRule")
                .bean(MorabeheUpdateLoanRuleHandler.class, "handle");
        // @formatter:on
    }

}
