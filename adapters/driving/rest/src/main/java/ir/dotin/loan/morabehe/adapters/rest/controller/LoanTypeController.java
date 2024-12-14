package ir.dotin.loan.morabehe.adapters.rest.controller;

import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanTypeCommand;
import ir.dotin.loan.morabehe.core.application.service.config.route.LoanTypeRoutes;
import ir.dotin.loan.morabehe.core.application.service.response.LoanTypeResponse;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(LoanTypeRoutes.ApiEndpoints.BASE_PATH)
public class LoanTypeController {

    private final ProducerTemplate producerTemplate;

    public LoanTypeController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }


    @PostMapping(LoanTypeRoutes.ApiEndpoints.CREATE)
    public ResponseEntity<LoanTypeResponse> createLoanType(@RequestBody MorabeheCreateLoanTypeCommand command){
        LoanTypeResponse response = producerTemplate.requestBody(
                LoanTypeRoutes.CREATE_LOAN_TYPE_URI, command, LoanTypeResponse.class);

        return ResponseEntity.ok(response);
    }

    /**
     * Compensation Endpoints
     */
    @PostMapping(LoanTypeRoutes.ApiEndpoints.COMPENSATE_CREATE)
    public ResponseEntity<LoanTypeResponse> compensateCreateLoanType(
            @RequestBody MorabeheCreateLoanTypeCommand command) {
        LoanTypeResponse response = producerTemplate.requestBody(
                LoanTypeRoutes.COMPENSATE_CREATE_LOAN_TYPE_URI, command, LoanTypeResponse.class);
        return ResponseEntity.ok(response);
    }

}
