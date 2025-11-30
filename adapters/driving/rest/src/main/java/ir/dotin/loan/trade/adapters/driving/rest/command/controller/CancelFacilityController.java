package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.adapter.rest.request.DataRequest;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.CancelFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.CancelFacilityRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/cancel")
@Tag(name = SwaggerConfig.TAG_FACILITY_CANCELLATION, description = "عملیات مربوط به لغو تسهیلات")
@RequiredArgsConstructor
@Hidden
class CancelFacilityController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final CancelFacilityRequestToCommandMapper mapper;

    @PostMapping
    @Operation(summary = "لغو تسهیلات")
    public EventStreamResponse cancelFacility(
            @Parameter(
                            description = "شناسه یکتای تسهیلات جهت لغو",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات درخواست لغو", required = true) @RequestBody
                    DataRequest<CancelFacilityRequest> request) {
        var command = mapper.toCommand(facilityId, request.payload());
        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
