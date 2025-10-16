package ir.dotin.loan.trade.adapters.driving.rest.query.loantype;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.base.ServiceResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.ports.outbound.query.response.GetLoanTypeByIdQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/loan-types")
@RequiredArgsConstructor
@Tag(name = "Loan Type Queries", description = "Query loan type")
public class LoanTypeQueryController {
    private final QueryDispatcher dispatcher;

    @GetMapping("/{loanTypeId}")
    @Operation(summary = "Get loanType by ID")
    public ResponseEntity<ServiceResponse<TradeLoanTypeQueryDto>> getById(
            @PathVariable UUID loanTypeId, @RequestHeader(value = "X-Request-ID") UUID uid) {
        GetLoanTypeByIdQuery query =
                GetLoanTypeByIdQuery.builder().uid(uid).loanTypeId(loanTypeId).build();
        ServiceResponse<TradeLoanTypeQueryDto> serviceResponse = ServiceResponse.<TradeLoanTypeQueryDto>builder()
                .data(dispatcher.dispatch(query))
                .build();
        return ResponseEntity.ok(serviceResponse);
    }
}
