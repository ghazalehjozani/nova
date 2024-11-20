package ir.dotin.loan.morabehe.core.application.service.route;

import ir.dotin.loan.morabehe.core.application.service.handler.CreateLoanRuleHandler;
import ir.dotin.platform.ddd.common.exception.DomainException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class LoanTypeCommandProcessorRoute extends RouteBuilder {

    @Override
    public void configure() {
        // @formatter:off
        onException(Exception.class)
                .handled(false)
                .log("Domain Exception: ${exception.message}");

        from("direct:loanRuleCommandProcessor")
                .choice()
                    .when(simple("${body.type} == 'CreateLoanRuleCommand'"))
                        .to("direct:createLoanRule")
                    .otherwise()
                        .log("Unknown command type: ${body.type}")
                .end();

        from("direct:createLoanRule")
                .bean(CreateLoanRuleHandler.class, "handle");
        // @formatter:on
    }
}
