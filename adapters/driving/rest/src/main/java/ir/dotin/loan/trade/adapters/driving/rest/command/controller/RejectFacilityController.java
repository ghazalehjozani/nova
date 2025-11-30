package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.adapter.rest.request.DataRequest;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.RejectFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.RejectFacilityRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/facilities/{facilityId}/reject")
@Tag(name = SwaggerConfig.TAG_FACILITY_REJECTION, description = "عملیات مربوط به رد تسهیلات")
@RequiredArgsConstructor
@Hidden
class RejectFacilityController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final RejectFacilityRequestToCommandMapper mapper;

    @PostMapping(version = "1+")
    @Operation(summary = "رد تسهیلات")
    public EventStreamResponse rejectFacility(
            @Parameter(
                            description = "شناسه یکتای تسهیلات جهت رد",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات رد تسهیلات", required = true) @RequestBody
                    DataRequest<RejectFacilityRequest> request) {
        var command = mapper.toCommand(facilityId, request.payload());
        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
