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
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.FullLifecycleRevertRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.FullLoanFacilityLifecycleRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.FullLoanFacilityLifecycleRequestMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLifecycleRevertCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/facilities/full-lifecycle")
@Tag(name = SwaggerConfig.TAG_FULL_LIFECYCLE, description = "عملیات چرخه کامل تسهیلات")
@RequiredArgsConstructor
class FullLoanFacilityLifecycleController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final FullLoanFacilityLifecycleRequestMapper mapper;
    private final AuthenticationContextHolder authenticationContextHolder;

    @PostMapping(version = "1+")
    @Operation(summary = "اجرای چرخه کامل تسهیلات از تشکیل تا پرداخت")
    public EventStreamResponse executeFullLifecycle(
            @RequestBody @Valid DataRequest<FullLoanFacilityLifecycleRequest> request) {

        var command = mapper.toCommand(request.payload()).toBuilder()
                .uid(getXRequestId())
                .transactionMetadata(buildTransactionContext())
                .build();

        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }

    private FullLoanFacilityLifecycleCommand.TransactionMetadataDto buildTransactionContext() {
        return FullLoanFacilityLifecycleCommand.TransactionMetadataDto.builder()
                .branchCode(authenticationContextHolder.branchCode().orElseThrow())
                .userId(authenticationContextHolder.userIdOrThrow())
                .terminalIp(authenticationContextHolder.ipAddress().orElseThrow())
                .terminalId("1")
                .productCode("LOAN")
                .channel("Branch")
                .networkType("BankBook")
                .terminalType("Branch")
                .toolSource("BANK")
                .build();
    }

    @PostMapping(value = "{facilityId}/compensate", version = "1+")
    @Operation(summary = "بازگشت کامل چرخه تسهیلات")
    public EventStreamResponse revertFullLifecycle(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @Parameter(description = "جزئیات درخواست بازگشت", required = true) @RequestBody @Valid
                    DataRequest<FullLifecycleRevertRequest> request) {

        var command = FullLifecycleRevertCommand.builder()
                .uid(getXRequestId())
                .version(request.payload().version())
                .loanFacilityId(facilityId)
                .reason(request.payload().reason())
                .contractTransactionNumberToReverse(request.payload().contractTransactionNumberToReverse())
                .disbursementTransactionNumberToReverse(request.payload().disbursementTransactionNumberToReverse())
                .collateralSerialsToRevert(request.payload().collateralSerialsToRevert())
                .build();

        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
