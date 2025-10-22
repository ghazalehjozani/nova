package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.headers.CommandEndpoint;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.IssueFacilityContractRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.IssueFacilityContractRequestToCommandMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/issue-contract")
@Tag(name = "عملیات صدور قرارداد", description = "عملیات مربوط به صدور قرارداد تسهیلات")
@RequiredArgsConstructor
@CommandEndpoint
public class IssueFacilityContractController {

    private final CommandDispatcher dispatcher;
    private final IssueFacilityContractRequestToCommandMapper mapper;

    @PostMapping
    @Operation(
            summary = "صدور قرارداد تسهیلات",
            description = "این عملیات قرارداد تسهیلات را صادر کرده و آماده اجرا می‌کند.")
    public EventStreamResponse issueFacilityContract(
            @Parameter(
                            description = "شناسه یکتای تسهیلات جهت صدور قرارداد",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات صدور قرارداد تسهیلات", required = true) @RequestBody
                    IssueFacilityContractRequest request) {
        var command = mapper.toCommand(facilityId, request);
        List<DomainEvent<?, ?>> domainEvents = dispatcher.dispatch(command);
        return EventStreamResponse.success(domainEvents);
    }
}
