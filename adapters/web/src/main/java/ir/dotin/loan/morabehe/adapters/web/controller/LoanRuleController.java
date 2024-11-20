package ir.dotin.loan.morabehe.adapters.web.controller;


import ir.dotin.loan.morabehe.core.application.service.command.CreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.command.UpdateLoanRuleCommand;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ResponseEntity<String> create(CreateLoanRuleCommand command) {
        String response = (String) producerTemplate
                .requestBody("direct:loanRuleCommandProcessor", command);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("update")
    public ResponseEntity<String> update(UpdateLoanRuleCommand command) {
        String response = (String) producerTemplate
                .requestBody("direct:loanRuleCommandProcessor");
        return ResponseEntity.ok(response);
    }

}
