package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.commons.security.AuthenticationContextHolder;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.OpenFacilityCaseRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.OpenFacilityCaseRequestToCommandMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OpenFacilityCaseCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/open-case")
@Tag(name = "عملیات ایجاد پرونده تسهیلات", description = "عملیات مربوط به ایجاد پرونده تسهیلات")
@RequiredArgsConstructor
public class OpenFacilityCaseController {

    private final CommandDispatcher dispatcher;
    private final OpenFacilityCaseRequestToCommandMapper mapper;
    private final AuthenticationContextHolder authenticationContextHolder;

    @PostMapping
    @Operation(
            summary = "ایجاد پرونده تسهیلات",
            description = "این عملیات یک پرونده تسهیلات جدید با تمام جزئیات لازم ایجاد می‌کند.")
    public EventStreamResponse openFacilityCase(
            @Parameter(description = "جزئیات درخواست باز کردن پرونده تسهیلات", required = true) @RequestBody
                    OpenFacilityCaseRequest request) {
        String branchCode = authenticationContextHolder.branchCode().orElse(null);
        var command = mapper.toCommand(request);
        var applicationDto = command.loanApplication().toBuilder()
                .branch(new OpenFacilityCaseCommand.BranchDto(branchCode))
                .build();
        var openFacilityCaseCommand =
                command.toBuilder().loanApplication(applicationDto).build();
        List<DomainEvent<?, ?>> domainEvents = dispatcher.dispatch(openFacilityCaseCommand);
        return EventStreamResponse.of(domainEvents);
    }
}
