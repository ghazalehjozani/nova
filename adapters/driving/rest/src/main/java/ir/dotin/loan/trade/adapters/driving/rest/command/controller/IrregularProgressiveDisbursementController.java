package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.Map;
import java.util.UUID;
import jakarta.validation.Valid;

import org.jspecify.annotations.Nullable;
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
import ir.dotin.loan.trade.adapters.driving.contract.dto.IrregularProgressiveDisbursementRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateIrregularDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IrregularProgressiveDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.AmountDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-facilities/{facilityId}/disburse/progressive-irregular")
@Tag(name = SwaggerConfig.TAG_IRREGULAR_DISBURSEMENT, description = "عملیات مربوط به پرداخت نامنظم تسهیلات")
@RequiredArgsConstructor
class IrregularProgressiveDisbursementController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final AuthenticationContextHolder authenticationContextHolder;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "پرداخت نامنظم")
    public ResponseEntity<Void> irregularDisbursement(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @Parameter(description = "جزئیات درخواست پرداخت نامنظم", required = true) @RequestBody @Valid
                    IrregularProgressiveDisbursementRequest requestBody) {

        Map<String, String> metadata = requestBody.metadata() == null ? Map.of() : requestBody.metadata();
        var command = IrregularProgressiveDisbursementCommand.builder()
                .uid(getIdempotencyKey())
                .loanFacilityId(facilityId)
                .trancheAmount(requestBody.trancheAmount())
                .version(requestBody.version())
                .installmentSchedulePlan(mapInstallmentPlan(requestBody.installmentSchedulePlan()))
                .branchCode(authenticationContextHolder.branchCode().orElseThrow())
                .userId(authenticationContextHolder.userIdOrThrow())
                .terminalIp(authenticationContextHolder.ipAddress().orElseThrow())
                .terminalId(metadata.get("terminalId"))
                .productCode(metadata.get("productCode"))
                .channel(metadata.get("channel"))
                .networkType(metadata.get("networkType"))
                .terminalType(metadata.get("terminalType"))
                .toolSource(metadata.get("toolSource"))
                .disbursementDate(requestBody.disbursementDate())
                .build();

        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }

    private IrregularProgressiveDisbursementCommand.@Nullable InstallmentSchedulePlanDto mapInstallmentPlan(
            IrregularProgressiveDisbursementRequest.@Nullable InstallmentSchedulePlanDto plan) {
        if (plan == null) return null;

        return IrregularProgressiveDisbursementCommand.InstallmentSchedulePlanDto.builder()
                .installments(plan.installments().stream()
                        .map(spec -> IrregularProgressiveDisbursementCommand.InstallmentSpecDto.builder()
                                .sequenceNumber(spec.sequenceNumber())
                                .dueDate(spec.dueDate())
                                .principalAmount(
                                        spec.interestAmount() != null ? new AmountDto(spec.principalAmount()) : null)
                                .interestAmount(
                                        spec.interestAmount() != null ? new AmountDto(spec.interestAmount()) : null)
                                .build())
                        .toList())
                .build();
    }

    @PostMapping(value = "/compensate", version = "1+")
    @Operation(summary = "جبران‌سازی مرحله پرداخت نامنظم")
    public ResponseEntity<Void> compensateIrregularDisbursement(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @RequestBody @Valid CompensationRequest request) {

        var command = CompensateIrregularDisbursementCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .loanFacilityId(facilityId)
                .build();

        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }
}
