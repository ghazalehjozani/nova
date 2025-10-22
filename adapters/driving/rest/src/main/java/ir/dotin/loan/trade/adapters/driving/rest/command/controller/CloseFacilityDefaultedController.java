package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.headers.CommandEndpoint;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.CloseFacilityDefaultedRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.CloseFacilityDefaultedRequestToCommandMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/close-defaulted")
@Tag(name = "عملیات بستن تسهیلات", description = "عملیات مربوط به بستن تسهیلات")
@RequiredArgsConstructor
@CommandEndpoint
public class CloseFacilityDefaultedController {

    private final CommandDispatcher dispatcher;
    private final CloseFacilityDefaultedRequestToCommandMapper mapper;

    @PostMapping
    @Operation(
            summary = "بستن تسهیلات",
            description = "این عملیات تسهیلات را به صورت نهایی می‌بندد و از انجام عملیات بیشتر جلوگیری می‌کند.")
    public EventStreamResponse closeFacilityDefaulted(
            @Parameter(
                            description = "شناسه یکتای تسهیلات معوق جهت بستن",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات عملیات بستن تسهیلات", required = true) @RequestBody
                    CloseFacilityDefaultedRequest request) {
        var command = mapper.toCommand(facilityId, request);
        List<DomainEvent<?, ?>> domainEvents = dispatcher.dispatch(command);
        return EventStreamResponse.success(domainEvents);
    }
}
