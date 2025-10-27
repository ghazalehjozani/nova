package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.adapter.rest.request.DataRequest;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.ApproveFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.ApproveFacilityRequestToCommandMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/approve")
@Tag(name = "عملیات تصویب مصوبه", description = "عملیات مربوط به تصویب مصوبه")
@RequiredArgsConstructor
public class ApproveFacilityController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final ApproveFacilityRequestToCommandMapper mapper;

    @PostMapping("/{sanctionSerial}")
    @Operation(summary = "تصویب مصوبه", description = "این عملیات با شماره سریال، مصوبه را تصویب می کند.")
    public EventStreamResponse approveFacility(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "شماره سریال یکتای مضوبه جهت تصویب", example = "SAN-2025-001", required = true)
                    @NotBlank
                    @PathVariable
                    String sanctionSerial,
            @Parameter(description = "جزئیات تصویب نصوبه", required = true) @RequestBody
                    DataRequest<ApproveFacilityRequest> request) {

        var command = mapper.toCommand(facilityId, sanctionSerial, request.payload());
        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
