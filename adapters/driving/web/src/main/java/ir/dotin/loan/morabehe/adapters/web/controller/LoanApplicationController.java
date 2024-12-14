package ir.dotin.loan.morabehe.adapters.web.controller;

import ir.dotin.loan.morabehe.core.application.service.command.MorabeheApproveLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.config.route.LoanApplicationRoutes;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(LoanApplicationRoutes.ApiEndpoints.BASE_PATH)
public class LoanApplicationController {

    private final ProducerTemplate producerTemplate;

    public LoanApplicationController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @PostMapping(LoanApplicationRoutes.ApiEndpoints.CREATE)
    public ResponseEntity<LoanApplicationResponse> createLoanApplication(
            @RequestBody MorabeheCreateLoanApplicationCommand command) {
        LoanApplicationResponse response = producerTemplate.requestBody(
                LoanApplicationRoutes.CREATE_LOAN_APPLICATION_URI, command, LoanApplicationResponse.class);
        return ResponseEntity.ok(response);
    }

    @PostMapping(LoanApplicationRoutes.ApiEndpoints.APPROVE)
    public ResponseEntity<LoanApplicationResponse> approveLoanApplication(
            @RequestBody MorabeheApproveLoanApplicationCommand command) {
        LoanApplicationResponse response = producerTemplate.requestBody(
                LoanApplicationRoutes.APPROVE_LOAN_APPLICATION_URI, command, LoanApplicationResponse.class);
        return ResponseEntity.ok(response);
    }

    /**
     * Compensation Endpoints
     */
    @PostMapping(LoanApplicationRoutes.ApiEndpoints.COMPENSATE_CREATE)
    public ResponseEntity<LoanApplicationResponse> compensateCreateLoanApplication(
            @RequestBody MorabeheCreateLoanApplicationCommand command) {
        LoanApplicationResponse response = producerTemplate.requestBody(
                LoanApplicationRoutes.COMPENSATE_CREATE_LOAN_APPLICATION_URI, command, LoanApplicationResponse.class);
        return ResponseEntity.ok(response);
    }

    @PostMapping(LoanApplicationRoutes.ApiEndpoints.COMPENSATE_APPROVE)
    public ResponseEntity<LoanApplicationResponse> compensateApproveLoanApplication(
            @RequestBody MorabeheApproveLoanApplicationCommand command) {
        LoanApplicationResponse response = producerTemplate.requestBody(
                LoanApplicationRoutes.COMPENSATE_APPROVE_LOAN_APPLICATION_URI, command, LoanApplicationResponse.class);
        return ResponseEntity.ok(response);
    }

}
