package ir.dotin.loan.morabehe.adapters.web.route;


import ir.dotin.loan.morabehe.adapters.web.exception.GlobalExceptionHandler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheUpdateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.handler.MorabeheCreateLoanRuleHandler;
import ir.dotin.loan.morabehe.core.application.service.handler.MorabeheUpdateLoanRuleHandler;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class LoanRuleRoute extends RouteBuilder {

    private final Environment env;
    private final GlobalExceptionHandler globalExceptionHandler;

    public LoanRuleRoute(Environment env, GlobalExceptionHandler globalExceptionHandler) {
        this.env = env;
        this.globalExceptionHandler = globalExceptionHandler;
    }


    @Override
    public void configure() throws Exception {
        // @formatter:off
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.auto)
                .contextPath("/api")
                .port(env.getProperty("server.port", "8085"))
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "LoanRule Rest API.")
                .apiProperty("api.version", "1.0")
                .apiProperty("cors", "false")
                .apiContextRouteId("doc-api");

        onException(Exception.class)
                .handled(true)
                .process(globalExceptionHandler);

        rest("/V1/loan-rule")
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

