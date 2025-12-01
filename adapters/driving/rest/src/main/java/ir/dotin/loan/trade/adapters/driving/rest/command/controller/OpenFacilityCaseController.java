package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.adapter.rest.request.DataRequest;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.commons.security.AuthenticationContextHolder;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.OriginateLoanFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.OriginateLoanFacilityRequestMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/facilities/open-case")
@Tag(name = SwaggerConfig.TAG_FACILITY_CASE_OPENING, description = "عملیات مربوط به ایجاد پرونده تسهیلات")
@RequiredArgsConstructor
class OpenFacilityCaseController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final OriginateLoanFacilityRequestMapper mapper;
    private final AuthenticationContextHolder authenticationContextHolder;

    @PostMapping(version = "1+")
    @Operation(summary = "ایجاد پرونده تسهیلات")
    public EventStreamResponse openFacilityCase(
            @Parameter(required = true) @Valid @RequestBody DataRequest<OriginateLoanFacilityRequest> request) {

        String branchCode = authenticationContextHolder.branchCode().orElse(null);

        OriginateLoanFacilityCommand command = mapper.toCommand(request.payload());

        OriginateLoanFacilityCommand enrichedCommand = command.toBuilder()
                .uid(getXRequestId())
                .loanApplication(command.loanApplication().toBuilder()
                        .branch(new OriginateLoanFacilityCommand.BranchDto(branchCode))
                        .build())
                .build();

        return EventStreamResponse.of(unwrap(dispatcher.dispatch(enrichedCommand)));
    }
}
