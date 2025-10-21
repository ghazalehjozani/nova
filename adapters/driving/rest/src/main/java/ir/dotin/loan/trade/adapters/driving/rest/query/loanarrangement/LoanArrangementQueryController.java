package ir.dotin.loan.trade.adapters.driving.rest.query.loanarrangement;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.rest.headers.QueryEndpoint;
import ir.dotin.platform.adapter.rest.response.DataResponse;
import ir.dotin.platform.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.application.ports.outbound.query.response.GetLoanArrangementByIdQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/loan-arrangements")
@RequiredArgsConstructor
@Tag(name = "Loan Arrangement Queries", description = "Query loan Arrangement")
@QueryEndpoint
public class LoanArrangementQueryController {
    private final QueryDispatcher dispatcher;

    @GetMapping("/{loanArrangementId}")
    @Operation(summary = "Get LoanArrangement by ID")
    public DataResponse<TradeLoanArrangementQueryDto> getById(@PathVariable UUID loanArrangementId) {
        GetLoanArrangementByIdQuery query = GetLoanArrangementByIdQuery.builder()
                .loanArrangementId(loanArrangementId)
                .build();
        return DataResponse.success(dispatcher.dispatch(query));
    }
}
