package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

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
import ir.dotin.loan.trade.adapters.driving.contract.dto.FullLifecycleRevertRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLifecycleRevertCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-facilities/{facilityId}/full-lifecycle")
@Tag(name = SwaggerConfig.TAG_FULL_LIFECYCLE, description = "عملیات چرخه کامل تسهیلات")
@RequiredArgsConstructor
class FullLoanFacilityLifecycleController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final CommandResponseFactory responseFactory;

    @PostMapping(value = "/compensate", version = "1+")
    @Operation(summary = "بازگشت کامل چرخه تسهیلات")
    public ResponseEntity<Void> revertFullLifecycle(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @Parameter(description = "جزئیات درخواست بازگشت", required = true) @RequestBody @Valid
                    FullLifecycleRevertRequest request) {

        var command = FullLifecycleRevertCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .loanFacilityId(facilityId)
                .reason(request.reason())
                .contractTransactionNumberToReverse(request.contractTransactionNumberToReverse())
                .disbursementTransactionNumberToReverse(request.disbursementTransactionNumberToReverse())
                .collateralSerialsToRevert(request.collateralSerialsToRevert())
                .build();

        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }
}
