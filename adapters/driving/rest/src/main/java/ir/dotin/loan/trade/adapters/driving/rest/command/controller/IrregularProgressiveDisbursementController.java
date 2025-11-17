package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.adapter.rest.request.DataRequest;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.commons.security.AuthenticationContextHolder;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.IrregularProgressiveDisbursementRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IrregularProgressiveDisbursementCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/disburse/progressive-irregular")
@Tag(name = SwaggerConfig.TAG_IRREGULAR_DISBURSEMENT, description = "عملیات مربوط به پرداخت نامنظم تسهیلات")
@RequiredArgsConstructor
public class IrregularProgressiveDisbursementController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final AuthenticationContextHolder authenticationContextHolder;

    @PostMapping
    @Operation(summary = "پرداخت نامنظم")
    public EventStreamResponse irregularDisbursement(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @Parameter(description = "جزئیات درخواست پرداخت نامنظم", required = true) @RequestBody @Valid
                    DataRequest<IrregularProgressiveDisbursementRequest> requestBody) {

        var payload = requestBody.payload();

        var command = IrregularProgressiveDisbursementCommand.builder()
                .loanFacilityId(facilityId)
                .trancheAmount(payload.trancheAmount())
                .version(payload.version())
                .installmentSchedulePlan(mapInstallmentPlan(payload.installmentSchedulePlan()))
                .branchCode(authenticationContextHolder.branchCode().orElseThrow())
                .userId(authenticationContextHolder.userIdOrThrow())
                .terminalIp(authenticationContextHolder.ipAddress().orElseThrow())
                .productCode("LOAN")
                .channel("Branch")
                .networkType("BankBook")
                .terminalType("Branch")
                .toolSource("BANK")
                .build();

        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }

    private IrregularProgressiveDisbursementCommand.InstallmentSchedulePlanDto mapInstallmentPlan(
            IrregularProgressiveDisbursementRequest.InstallmentSchedulePlanDto plan) {
        if (plan == null) return null;

        return IrregularProgressiveDisbursementCommand.InstallmentSchedulePlanDto.builder()
                .installments(plan.installments().stream()
                        .map(spec -> IrregularProgressiveDisbursementCommand.InstallmentSpecDto.builder()
                                .sequenceNumber(spec.sequenceNumber())
                                .dueDate(spec.dueDate())
                                .principalAmount(spec.principalAmount())
                                .interestAmount(spec.interestAmount())
                                .build())
                        .toList())
                .build();
    }
}
