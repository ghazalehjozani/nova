package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.List;
import java.util.UUID;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.RejectFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.RejectFacilityRequestToCommandMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/reject")
@Tag(name = "عملیات رد تسهیلات", description = "عملیات مربوط به رد تسهیلات")
@RequiredArgsConstructor
public class RejectFacilityController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final RejectFacilityRequestToCommandMapper mapper;

    @PostMapping
    @Operation(
            summary = "رد تسهیلات",
            description = "این عملیات تسهیلات را به عنوان رد شده علامت‌گذاری کرده و از ادامه فرآیند جلوگیری می‌کند.")
    public EventStreamResponse rejectFacility(
            @Parameter(
                            description = "شناسه یکتای تسهیلات جهت رد",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات رد تسهیلات", required = true) @RequestBody RejectFacilityRequest request) {
        var command = mapper.toCommand(facilityId, request);
        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
