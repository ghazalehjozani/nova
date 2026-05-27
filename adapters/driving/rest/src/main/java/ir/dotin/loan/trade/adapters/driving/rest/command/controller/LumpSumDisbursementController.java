package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CompensationRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.LumpSumDisbursementRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateLumpSumDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LumpSumDisbursementCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-facilities/{facilityId}/disburse/lump-sum")
@Tag(name = SwaggerConfig.TAG_LUMP_SUM_DISBURSEMENT, description = "عملیات مربوط به پرداخت یکجای تسهیلات")
@RequiredArgsConstructor
class LumpSumDisbursementController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final AuthenticationContextHolder authenticationContextHolder;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "پرداخت یکجا")
    public ResponseEntity<Void> lumpSumDisbursement(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات درخواست پرداخت یکجا", required = true) @RequestBody
                    LumpSumDisbursementRequest requestBody) {
        var lumpSumDisbursementCommand = LumpSumDisbursementCommand.builder()
                .uid(getIdempotencyKey())
                .version(requestBody.version())
                .loanFacilityId(facilityId)
                .branchCode(authenticationContextHolder.branchCode().orElseThrow())
                .productCode("LOAN")
                .channel("Branch")
                .networkType("BankBook")
                .terminalIp(authenticationContextHolder.ipAddress().orElseThrow())
                .terminalType("Branch")
                .toolSource("BANK")
                .disbursementDate(Objects.requireNonNullElse(requestBody.disbursementDate(), LocalDate.now()))
                .userId(authenticationContextHolder.userIdOrThrow())
                .build();
        var result = dispatcher.dispatch(lumpSumDisbursementCommand);
        return responseFactory.mutated(result);
    }

    @PostMapping(value = "/compensate", version = "1+")
    @Operation(summary = "جبران‌سازی مرحله پرداخت یکجا")
    public ResponseEntity<Void> compensateLumpSumDisbursement(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @RequestBody @Valid CompensationRequest request) {

        var command = CompensateLumpSumDisbursementCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .loanFacilityId(facilityId)
                .build();

        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }
}
