package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.protocol.api.response.BaseResponse;
import ir.dotin.platform.protocol.api.response.EventStream;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.CloseFacilityDefaultedRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.CloseFacilityDefaultedRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/facilities/{facilityId}/close-defaulted")
@Tag(name = SwaggerConfig.TAG_FACILITY_CLOSURE_DEFAULTED, description = "عملیات مربوط به بستن تسهیلات معوق")
@RequiredArgsConstructor
class CloseFacilityDefaultedController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final CloseFacilityDefaultedRequestToCommandMapper mapper;

    @PostMapping(version = "1+")
    @Operation(summary = "بستن تسهیلات معوق")
    public ResponseEntity<BaseResponse<EventStream>> closeFacilityDefaulted(
            @Parameter(
                            description = "شناسه یکتای تسهیلات معوق جهت بستن",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات عملیات بستن تسهیلات", required = true) @RequestBody
                    CloseFacilityDefaultedRequest request) {
        var command = mapper.toCommand(facilityId, request);
        return ResponseEntity.ok(BaseResponse.success(EventStream.of(unwrap(dispatcher.dispatch(command)))));
    }
}
