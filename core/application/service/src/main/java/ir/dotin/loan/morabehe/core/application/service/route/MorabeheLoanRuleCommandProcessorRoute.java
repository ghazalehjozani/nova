package ir.dotin.loan.morabehe.core.application.service.route;

import ir.dotin.loan.morabehe.core.application.service.handler.MorabeheCreateLoanRuleHandler;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MorabeheLoanRuleCommandProcessorRoute extends RouteBuilder {

    @Override
    public void configure() {
        // @formatter:off
        onException(Exception.class)
                .handled(false)
                .log("Domain Exception: ${exception.message}");

        from("direct:morabeheLoanRuleCommandProcessor")
                .choice()
                    .when(simple("${body.type} == 'MorabeheCreateLoanRuleCommand'"))
                        .to("direct:createMorabeheLoanRule")
                    .otherwise()
                        .log("Unknown command type: ${body.type}")
                .end();

        from("direct:createMorabeheLoanRule")
                .bean(MorabeheCreateLoanRuleHandler.class, "handle");
        // @formatter:on
    }
}
