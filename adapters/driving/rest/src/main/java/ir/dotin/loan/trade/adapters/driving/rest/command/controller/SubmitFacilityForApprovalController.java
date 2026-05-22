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
import ir.dotin.platform.pangaea.protocol.api.response.BaseResponse;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CompensationRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.SubmitFacilityForApprovalRequest;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.SubmitFacilityForApprovalRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalSubmissionCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/facilities/{facilityId}/submit-for-approval")
@Tag(name = SwaggerConfig.TAG_FACILITY_APPROVAL_SUBMISSION, description = "عملیات مربوط به ثبت درخواست تصویب مصوبه")
@RequiredArgsConstructor
class SubmitFacilityForApprovalController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final SubmitFacilityForApprovalRequestToCommandMapper mapper;

    @PostMapping(version = "1+")
    @Operation(summary = "ثبت درخواست تصویب تسهیلات")
    public ResponseEntity<BaseResponse<Void>> submitFacilityForApproval(
            @PathVariable UUID facilityId,
            @Parameter(description = "جزئیات ثبت درخواست تصویب مصوبه", required = true) @RequestBody @Valid
                    SubmitFacilityForApprovalRequest request) {
        var command = mapper.toCommand(facilityId, request);
        dispatcher.dispatch(command);
        return ResponseEntity.ok(BaseResponse.success());
    }

    @PostMapping(value = "/compensate", version = "1+")
    @Operation(summary = "جبران‌سازی مرحله ثبت درخواست تصویب")
    public ResponseEntity<BaseResponse<Void>> compensateApprovalSubmission(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @RequestBody @Valid CompensationRequest request) {

        var command = CompensateApprovalSubmissionCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .loanFacilityId(facilityId)
                .build();

        dispatcher.dispatch(command);
        return ResponseEntity.ok(BaseResponse.success());
    }
}
