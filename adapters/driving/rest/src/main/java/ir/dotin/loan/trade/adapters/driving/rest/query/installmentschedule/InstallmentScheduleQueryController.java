package ir.dotin.loan.trade.adapters.driving.rest.query.installmentschedule;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.base.ServiceResponse;
import ir.dotin.loan.trade.core.application.ports.inbound.query.GetInstallmentScheduleByIdQuery;
import ir.dotin.loan.trade.core.application.ports.outbound.query.dto.TradeInstallmentScheduleQueryDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/installment-schedules")
@RequiredArgsConstructor
@Tag(name = "Installment Schedule Queries", description = "Query Installment Schedule ")
public class InstallmentScheduleQueryController {

    private final QueryDispatcher dispatcher;

    @GetMapping("/{installmentScheduleId}")
    @Operation(summary = "Get installmentSchedule by ID")
    public ResponseEntity<ServiceResponse<TradeInstallmentScheduleQueryDto>> getById(
            @PathVariable UUID installmentScheduleId, @RequestHeader(value = "X-Request-ID") UUID uid) {
        GetInstallmentScheduleByIdQuery query = GetInstallmentScheduleByIdQuery.builder()
                .uid(uid)
                .installmentScheduleId(installmentScheduleId)
                .build();
        ServiceResponse<TradeInstallmentScheduleQueryDto> serviceResponse =
                ServiceResponse.<TradeInstallmentScheduleQueryDto>builder()
                        .data(dispatcher.dispatch(query))
                        .build();
        return ResponseEntity.ok(serviceResponse);
    }
}
