package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;

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
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.LumpSumDisbursementRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LumpSumDisbursementCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/disburse/lump-sum")
@Tag(name = SwaggerConfig.TAG_LUMP_SUM_DISBURSEMENT, description = "عملیات مربوط به پرداخت یکجای تسهیلات")
@RequiredArgsConstructor
class LumpSumDisbursementController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final AuthenticationContextHolder authenticationContextHolder;

    @PostMapping
    @Operation(summary = "پرداخت یکجا")
    public EventStreamResponse lumpSumDisbursement(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات درخواست پرداخت یکجا", required = true) @RequestBody
                    DataRequest<LumpSumDisbursementRequest> requestBody) {
        var lumpSumDisbursementCommand = LumpSumDisbursementCommand.builder()
                .version(requestBody.payload().version())
                .loanFacilityId(facilityId)
                .branchCode(authenticationContextHolder.branchCode().orElseThrow())
                .productCode("LOAN")
                .channel("Branch")
                .networkType("BankBook")
                .terminalIp(authenticationContextHolder.ipAddress().orElseThrow())
                .terminalType("Branch")
                .toolSource("BANK")
                .userId(authenticationContextHolder.userIdOrThrow())
                .build();
        return EventStreamResponse.of(unwrap(dispatcher.dispatch(lumpSumDisbursementCommand)));
    }
}
