package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.pangaea.protocol.api.response.BaseResponse;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CompensationRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateLoanFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.OriginateLoanFacilityRequestMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateOriginationCommand;
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
    public ResponseEntity<BaseResponse<Void>> openFacilityCase(
            @Parameter(required = true) @Valid @RequestBody OriginateLoanFacilityRequest request) {

        String branchCode = authenticationContextHolder.branchCode().orElse(null);

        OriginateLoanFacilityCommand command = mapper.toCommand(request);

        OriginateLoanFacilityCommand enrichedCommand = command.toBuilder()
                .uid(getIdempotencyKey())
                .loanApplication(command.loanApplication().toBuilder()
                        .branch(new OriginateLoanFacilityCommand.BranchDto(branchCode))
                        .build())
                .build();
        dispatcher.dispatch(enrichedCommand);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success());
    }

    @PostMapping(value = "/{facilityId}/compensate", version = "1+")
    @Operation(summary = "جبران‌سازی مرحله تشکیل پرونده")
    public ResponseEntity<BaseResponse<Void>> compensateOrigination(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @RequestBody @Valid CompensationRequest request) {

        var command = CompensateOriginationCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .loanFacilityId(facilityId)
                .reason(request.reason())
                .build();

        dispatcher.dispatch(command);
        return ResponseEntity.ok(BaseResponse.success());
    }
}
