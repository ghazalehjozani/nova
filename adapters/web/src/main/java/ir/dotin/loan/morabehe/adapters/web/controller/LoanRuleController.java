package ir.dotin.loan.morabehe.adapters.web.controller;


import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.config.route.LoanRuleRoutes;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(LoanRuleRoutes.ApiEndpoints.BASE_PATH)
public class LoanRuleController {

    private final ProducerTemplate producerTemplate;

    public LoanRuleController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }


    @PostMapping(LoanRuleRoutes.ApiEndpoints.CREATE)
    public ResponseEntity<LoanRuleResponse> createLoanRule(@RequestBody MorabeheCreateLoanRuleCommand command){
        LoanRuleResponse response = producerTemplate.requestBody(
                LoanRuleRoutes.CREATE_LOAN_RULE_URI, command, LoanRuleResponse.class);
        return ResponseEntity.ok(response);
    }

    /**
     * Compensation Endpoints
     */
    @PostMapping(LoanRuleRoutes.ApiEndpoints.COMPENSATE_CREATE)
    public ResponseEntity<LoanRuleResponse> compensateCreateLoanRule(
            @RequestBody MorabeheCreateLoanRuleCommand command) {
        LoanRuleResponse response = producerTemplate.requestBody(
                LoanRuleRoutes.COMPENSATE_CREATE_LOAN_RULE_URI, command, LoanRuleResponse.class);
        return ResponseEntity.ok(response);
    }
}
