package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CompensationRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateEqualInstallmentFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateUnequalInstallmentFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.OriginateEqualInstallmentFacilityRequestMapper;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.OriginateUnequalInstallmentFacilityRequestMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateOriginationCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateEqualInstallmentFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateUnequalInstallmentFacilityCommand;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-facilities")
@Tag(name = SwaggerConfig.TAG_FACILITY_CASE_OPENING, description = "عملیات مربوط به ایجاد پرونده تسهیلات")
@RequiredArgsConstructor
class OpenFacilityCaseController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final OriginateEqualInstallmentFacilityRequestMapper equalInstallmentMapper;
    private final OriginateUnequalInstallmentFacilityRequestMapper unequalInstallmentMapper;
    private final AuthenticationContextHolder authenticationContextHolder;
    private final CommandResponseFactory responseFactory;

    @PostMapping(value = "/equal-installments", version = "1+")
    @Operation(summary = "ایجاد پرونده تسهیلات با اقساط مساوی یا یکجا")
    public ResponseEntity<Void> openEqualInstallmentFacilityCase(
            @Parameter(required = true) @Valid @RequestBody OriginateEqualInstallmentFacilityRequest request) {

        OriginateEqualInstallmentFacilityCommand command = equalInstallmentMapper.toCommand(request);

        var result = dispatcher.dispatch(command.toBuilder()
                .uid(getIdempotencyKey())
                .loanApplication(withBranch(command))
                .build());

        return responseFactory.created(result, "loan-facilities");
    }

    @PostMapping(value = "/unequal-installments", version = "1+")
    @Operation(summary = "ایجاد پرونده تسهیلات با اقساط نامساوی")
    public ResponseEntity<Void> openUnequalInstallmentFacilityCase(
            @Parameter(required = true) @Valid @RequestBody OriginateUnequalInstallmentFacilityRequest request) {

        OriginateUnequalInstallmentFacilityCommand command = unequalInstallmentMapper.toCommand(request);

        var result = dispatcher.dispatch(command.toBuilder()
                .uid(getIdempotencyKey())
                .loanApplication(withBranch(command))
                .build());

        return responseFactory.created(result, "loan-facilities");
    }

    @PostMapping(value = "/{facilityId}/origination/compensate", version = "1+")
    @Operation(summary = "جبران‌سازی مرحله تشکیل پرونده")
    @Hidden
    public ResponseEntity<Void> compensateOrigination(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @RequestBody @Valid CompensationRequest request) {

        var command = CompensateOriginationCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .loanFacilityId(facilityId)
                .reason(request.reason())
                .build();

        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }

    private OriginateFacilityCommand.LoanApplicationDto withBranch(OriginateFacilityCommand command) {
        String branchCode = authenticationContextHolder.branchCode().orElse(null);
        return command.loanApplication().toBuilder()
                .branch(new OriginateFacilityCommand.BranchDto(branchCode))
                .build();
    }
}
