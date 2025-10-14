package ir.dotin.loan.trade.adapters.driving.rest.query.loanarrangement;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.base.ServiceResponse;
import ir.dotin.loan.trade.core.application.ports.inbound.query.GetLoanArrangementByIdQuery;
import ir.dotin.loan.trade.core.application.ports.outbound.query.dto.TradeLoanArrangementQueryDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/loan-arrangements")
@RequiredArgsConstructor
@Tag(name = "Loan Arrangement Queries", description = "Query loan Arrangement")
public class LoanArrangementQueryController {
    private final QueryDispatcher dispatcher;

    @GetMapping("/{loanArrangementId}")
    @Operation(summary = "Get LoanArrangement by ID")
    public ResponseEntity<ServiceResponse<TradeLoanArrangementQueryDto>> getById(
            @PathVariable UUID loanArrangementId, @RequestHeader(value = "X-Request-ID") UUID uid) {
        GetLoanArrangementByIdQuery query = GetLoanArrangementByIdQuery.builder()
                .uid(uid)
                .loanArrangementId(loanArrangementId)
                .build();
        ServiceResponse<TradeLoanArrangementQueryDto> serviceResponse =
                ServiceResponse.<TradeLoanArrangementQueryDto>builder()
                        .data(dispatcher.dispatch(query))
                        .build();
        return ResponseEntity.ok(serviceResponse);
    }
}
