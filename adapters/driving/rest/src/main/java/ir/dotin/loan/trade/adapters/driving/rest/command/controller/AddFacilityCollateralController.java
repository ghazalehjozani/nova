package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.adapter.rest.request.DataRequest;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.AddFacilityCollateralRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.CompensateCollateralRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.AddFacilityCollateralRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollateralCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/facilities/{facilityId}/collaterals")
@Tag(name = SwaggerConfig.TAG_FACILITY_COLLATERAL_MANAGEMENT, description = "عملیات مربوط به مدیریت وثایق تسهیلات")
@RequiredArgsConstructor
class AddFacilityCollateralController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final AddFacilityCollateralRequestToCommandMapper mapper;

    @PostMapping(version = "1+")
    @Operation(summary = "افزودن وثیقه")
    public EventStreamResponse addCollaterals(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات افزودن وثیقه به تسهیلات", required = true) @RequestBody
                    DataRequest<AddFacilityCollateralRequest> request) {
        var command = mapper.toCommand(facilityId, request.payload()).toBuilder()
                .uid(getXRequestId())
                .build();
        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }

    @PostMapping(value = "/compensate", version = "1+")
    @Operation(summary = "جبران‌سازی افزودن وثایق")
    public EventStreamResponse compensateAddCollaterals(
            @Parameter(description = "شناسه تسهیلات", required = true) @PathVariable UUID facilityId,
            @Parameter(description = "جزئیات وثایق برای جبران‌سازی", required = true) @RequestBody
                    DataRequest<CompensateCollateralRequest> request) {

        var command = CompensateCollateralCommand.builder()
                .uid(getXRequestId())
                .loanFacilityId(facilityId)
                .collateralSerials(request.payload().collateralSerials())
                .build();

        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
