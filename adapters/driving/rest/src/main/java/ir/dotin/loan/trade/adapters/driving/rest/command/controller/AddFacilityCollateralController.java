package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

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
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.AddFacilityCollateralRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/collateral")
@Tag(name = SwaggerConfig.TAG_FACILITY_COLLATERAL_MANAGEMENT, description = "عملیات مربوط به مدیریت وثایق تسهیلات")
@RequiredArgsConstructor
class AddFacilityCollateralController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final AddFacilityCollateralRequestToCommandMapper mapper;

    @PostMapping("/{collateralSerial}")
    @Operation(summary = "افزودن وثیقه")
    public EventStreamResponse addCollateral(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "شماره سریال یکتای وثیقه جهت افزودن", example = "COLL-2025-001", required = true)
                    @NotBlank
                    @PathVariable
                    String collateralSerial,
            @Parameter(description = "جزئیات افزودن وثیقه به تسهیلات", required = true) @RequestBody
                    DataRequest<AddFacilityCollateralRequest> request) {
        var command = mapper.toCommand(facilityId, collateralSerial, request.payload()).toBuilder()
                .uid(getXRequestId())
                .build();
        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
