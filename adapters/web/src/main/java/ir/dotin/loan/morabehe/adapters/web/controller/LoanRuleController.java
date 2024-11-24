package ir.dotin.loan.morabehe.adapters.web.controller;


import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("loan-rule")
public class LoanRuleController {


    private final ProducerTemplate producerTemplate;

    public LoanRuleController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @PostMapping("create")
    public ResponseEntity<LoanRuleResponse> create(
            @RequestBody MorabeheCreateLoanRuleCommand command) {
        LoanRuleResponse response = producerTemplate
                .requestBody("direct:morabeheLoanRuleCommandProcessor",
                command, LoanRuleResponse.class);
        return ResponseEntity.ok(response);
    }

}
