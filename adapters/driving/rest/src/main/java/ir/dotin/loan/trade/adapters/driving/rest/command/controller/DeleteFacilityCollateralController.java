package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DeleteFacilityCollateralRequest;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.DeleteFacilityCollateralRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-facilities/{facilityId}/collaterals")
@Tag(name = SwaggerConfig.TAG_FACILITY_COLLATERAL_MANAGEMENT, description = "عملیات مربوط به مدیریت وثایق تسهیلات")
@RequiredArgsConstructor
class DeleteFacilityCollateralController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final DeleteFacilityCollateralRequestToCommandMapper mapper;
    private final CommandResponseFactory responseFactory;

    @DeleteMapping(version = "1+")
    @Operation(summary = "حذف وثیقه")
    public ResponseEntity<Void> deleteCollaterals(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات حذف وثایق تسهیلات", required = true) @RequestBody
                    DeleteFacilityCollateralRequest request) {
        var command = mapper.toDeleteCommand(facilityId, request).toBuilder()
                .uid(getIdempotencyKey())
                .build();
        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }
}
