package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.pangaea.protocol.api.response.BaseResponse;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.loan.trade.adapters.driving.contract.dto.AddFacilityCollateralRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CompensateCollateralRequest;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.AddFacilityCollateralRequestToCommandMapper;
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
    public ResponseEntity<BaseResponse<Void>> addCollaterals(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات افزودن وثیقه به تسهیلات", required = true) @RequestBody
                    AddFacilityCollateralRequest request) {
        var command = mapper.toCommand(facilityId, request).toBuilder()
                .uid(getIdempotencyKey())
                .build();
        dispatcher.dispatch(command);
        ;
        return ResponseEntity.ok(BaseResponse.success());
    }

    @PostMapping(value = "/compensate", version = "1+")
    @Operation(summary = "جبران‌سازی افزودن وثایق")
    public ResponseEntity<BaseResponse<Void>> compensateAddCollaterals(
            @Parameter(description = "شناسه تسهیلات", required = true) @PathVariable UUID facilityId,
            @Parameter(description = "جزئیات وثایق برای جبران‌سازی", required = true) @RequestBody
                    CompensateCollateralRequest request) {

        var command = CompensateCollateralCommand.builder()
                .uid(getIdempotencyKey())
                .loanFacilityId(facilityId)
                .collateralSerials(request.collateralSerials())
                .build();
        dispatcher.dispatch(command);
        ;
        return ResponseEntity.ok(BaseResponse.success());
    }
}
