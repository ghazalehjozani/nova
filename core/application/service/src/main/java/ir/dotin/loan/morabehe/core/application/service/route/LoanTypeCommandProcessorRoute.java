package ir.dotin.loan.morabehe.core.application.service.route;

import ir.dotin.loan.morabehe.core.application.service.handler.CreateLoanRuleHandler;
import ir.dotin.loan.morabehe.core.application.service.handler.UpdateLoanRuleHandler;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class LoanTypeCommandProcessorRoute extends RouteBuilder {

    @Override
    public void configure() {
        // @formatter:off
        from("direct:loanRuleCommandProcessor")
                .choice()
                    .when(simple("${body.type} == 'CreateLoanRuleCommand'"))
                        .to("direct:createLoanRule")
                    .when(simple("${body.type} == 'UpdateLoanRuleCommand'"))
                        .to("direct:updateLoanRule")
                    .otherwise()
                        .log("Unknown command type: ${body.type}")
                .end();

        from("direct:createLoanRule")
                .bean(CreateLoanRuleHandler.class, "handle");
        from("direct:updateLoanRule")
                .bean(UpdateLoanRuleHandler.class, "handle");
        // @formatter:on
    }
}
