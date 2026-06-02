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
import ir.dotin.loan.trade.adapters.driving.contract.dto.CompensationRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.SubmitFacilityForApprovalRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalSubmissionCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.SubmitFacilityForApprovalCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-facilities/{facilityId}/submit-for-approval")
@Tag(name = SwaggerConfig.TAG_FACILITY_APPROVAL_SUBMISSION, description = "عملیات مربوط به ثبت درخواست تصویب مصوبه")
@RequiredArgsConstructor
class SubmitFacilityForApprovalController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "ثبت درخواست تصویب تسهیلات")
    public ResponseEntity<Void> submitFacilityForApproval(
            @PathVariable UUID facilityId,
            @Parameter(description = "جزئیات ثبت درخواست تصویب مصوبه", required = true) @RequestBody @Valid
                    SubmitFacilityForApprovalRequest request) {
        // Idempotency/correlation key comes from the filter-populated InvocationContext (X-Correlation-ID /
        // Idempotency-Key), never from the request body, so it is consistent across every command controller.
        var command = new SubmitFacilityForApprovalCommand(
                getIdempotencyKey(), request.version().longValue(), facilityId);
        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }

    @PostMapping(value = "/compensate", version = "1+")
    @Operation(summary = "جبران‌سازی مرحله ثبت درخواست تصویب")
    public ResponseEntity<Void> compensateApprovalSubmission(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @RequestBody @Valid CompensationRequest request) {

        var command = CompensateApprovalSubmissionCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .loanFacilityId(facilityId)
                .build();

        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }
}
