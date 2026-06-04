package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.loan.trade.adapters.driving.contract.dto.ApproveFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CompensationRequest;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.ApproveFacilityRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.query.FacilityApprovalPreflightResult;
import ir.dotin.loan.trade.core.application.ports.inbound.query.PrepareFacilityApprovalQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-facilities/{facilityId}/approve")
@Tag(name = SwaggerConfig.TAG_FACILITY_APPROVAL, description = "عملیات مربوط به تصویب مصوبه")
@RequiredArgsConstructor
class ApproveFacilityController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final QueryDispatcher queryDispatcher;
    private final ApproveFacilityRequestToCommandMapper mapper;
    private final AuthenticationContextHolder authenticationContextHolder;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "تصویب خودکار مصوبه")
    public ResponseEntity<Void> autoApproveFacility(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات تصویب مصوبه", required = true) @RequestBody
                    ApproveFacilityRequest request) {

        var command = mapper.toCommand(facilityId, getIdempotencyKey(), null, request).toBuilder()
                .branchCode(authenticationContextHolder.branchCode().orElseThrow())
                .build();
        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }

    @PostMapping(value = "/{sanctionSerial}", version = "1+")
    @Operation(summary = "تصویب مصوبه با شماره سریال")
    public ResponseEntity<Void> approveFacilityWithSerial(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "شماره سریال یکتای مصوبه جهت تصویب", example = "SAN-2025-001", required = true)
                    @NotBlank
                    @PathVariable
                    String sanctionSerial,
            @Parameter(description = "جزئیات تصویب مصوبه", required = true) @RequestBody
                    ApproveFacilityRequest request) {
        ApproveFacilityCommand command =
                mapper.toCommand(facilityId, getIdempotencyKey(), sanctionSerial, request).toBuilder()
                        .branchCode(authenticationContextHolder.branchCode().orElseThrow())
                        .build();

        // Tx-free pre-flight (manual path only): the single FCB sanction-details read runs with no pooled connection
        // held (LN-59412). A failure throws FailureCauseException, which the platform advice maps to HTTP — do not
        // catch it. The resolved details are threaded onto the command so the transactional handler issues no FCB call.
        FacilityApprovalPreflightResult preflight = queryDispatcher.dispatch(new PrepareFacilityApprovalQuery(command));

        ApproveFacilityCommand preparedCommand =
                command.toBuilder().sanctionDetails(preflight.sanctionDetails()).build();

        var result = dispatcher.dispatch(preparedCommand);
        return responseFactory.mutated(result);
    }

    @PostMapping(value = "/compensate", version = "1+")
    @Operation(summary = "جبران‌سازی مرحله تصویب")
    public ResponseEntity<Void> compensateApproval(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @RequestBody @Valid CompensationRequest request) {

        var command = CompensateApprovalCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .loanFacilityId(facilityId)
                .build();
        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }
}
