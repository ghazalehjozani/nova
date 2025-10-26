package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.rest.headers.CommandEndpoint;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.SubmitFacilityForApprovalRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.SubmitFacilityForApprovalRequestToCommandMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/submit-for-approval")
@Tag(name = "ثبت درخواست تصویب مصوبه", description = "عملیات مربوط به ثبت درخواست تصویب مصوبه")
@RequiredArgsConstructor
@CommandEndpoint
public class SubmitFacilityForApprovalController {

    private final CommandDispatcher dispatcher;
    private final SubmitFacilityForApprovalRequestToCommandMapper mapper;

    @PostMapping
    @Operation(summary = "ثبت درخواست تصویب مصوبه", description = "این عملیات درخواست تصویب مصوبه را ثبت میکند.")
    public EventStreamResponse submitFacilityForApproval(
            @PathVariable UUID facilityId,
            @Parameter(description = "جزئیات ثبت درخواست تصویب مصوبه", required = true) @RequestBody
                    SubmitFacilityForApprovalRequest request) {
        var command = mapper.toCommand(facilityId, request);
        List<DomainEvent<?, ?>> domainEvents = dispatcher.dispatch(command);
        return EventStreamResponse.of(domainEvents);
    }
}
