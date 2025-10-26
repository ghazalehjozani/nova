package ir.dotin.loan.trade.adapters.driving.rest.query.loanfacility;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.rest.headers.QueryEndpoint;
import ir.dotin.platform.adapter.rest.response.DataResponse;
import ir.dotin.platform.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeFacilityQueryDto;
import ir.dotin.loan.trade.core.application.ports.outbound.query.response.GetFacilityByIdQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities")
@RequiredArgsConstructor
@Tag(name = "Facility Queries", description = "Query loan facilities")
@QueryEndpoint
public class FacilityQueryController {

    private final QueryDispatcher dispatcher;

    @GetMapping("/{facilityId}")
    @Operation(summary = "Get facility by ID")
    public DataResponse<TradeFacilityQueryDto> getById(@PathVariable UUID facilityId) {
        GetFacilityByIdQuery query =
                GetFacilityByIdQuery.builder().loanFacilityId(facilityId).build();
        return DataResponse.of(dispatcher.dispatch(query));
    }
}
