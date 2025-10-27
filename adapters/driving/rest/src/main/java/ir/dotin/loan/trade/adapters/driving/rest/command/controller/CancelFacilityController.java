package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.CancelFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.CancelFacilityRequestToCommandMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/cancel")
@Tag(name = "عملیات لغو تسهیلات", description = "عملیات مربوط به لغو تسهیلات")
@RequiredArgsConstructor
public class CancelFacilityController {

    private final CommandDispatcher dispatcher;
    private final CancelFacilityRequestToCommandMapper mapper;

    @PostMapping
    @Operation(
            summary = "لغو تسهیلات",
            description =
                    "این عملیات تسهیلات را به عنوان لغو شده علامت‌گذاری کرده و از انجام عملیات بیشتر جلوگیری می‌کند. ")
    public EventStreamResponse cancelFacility(
            @Parameter(
                            description = "شناسه یکتای تسهیلات جهت لغو",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات درخواست لغو", required = true) @RequestBody
                    CancelFacilityRequest request) {
        var command = mapper.toCommand(facilityId, request);
        List<DomainEvent<?, ?>> domainEvents = dispatcher.dispatch(command);
        return EventStreamResponse.of(domainEvents);
    }
}
