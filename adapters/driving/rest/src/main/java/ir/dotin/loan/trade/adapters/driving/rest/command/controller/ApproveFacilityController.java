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

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.protocol.api.response.BaseResponse;
import ir.dotin.platform.protocol.api.response.EventStream;
import ir.dotin.loan.trade.adapters.driving.contract.dto.ApproveFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CompensationRequest;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.ApproveFacilityRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/facilities/{facilityId}/approve")
@Tag(name = SwaggerConfig.TAG_FACILITY_APPROVAL, description = "عملیات مربوط به تصویب مصوبه")
@RequiredArgsConstructor
class ApproveFacilityController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final ApproveFacilityRequestToCommandMapper mapper;

    @PostMapping(version = "1+")
    @Operation(summary = "تصویب خودکار مصوبه")
    public ResponseEntity<BaseResponse<EventStream>> autoApproveFacility(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات تصویب مصوبه", required = true) @RequestBody
                    ApproveFacilityRequest request) {

        var command = mapper.toCommand(facilityId, null, request).toBuilder()
                .uid(getIdempotencyKey())
                .build();
        return ResponseEntity.ok(BaseResponse.success(EventStream.of(unwrap(dispatcher.dispatch(command)))));
    }

    @PostMapping(value = "/{sanctionSerial}", version = "1+")
    @Operation(summary = "تصویب مصوبه با شماره سریال")
    public ResponseEntity<BaseResponse<EventStream>> approveFacilityWithSerial(
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

        var command = mapper.toCommand(facilityId, sanctionSerial, request);
        return ResponseEntity.ok(BaseResponse.success(EventStream.of(unwrap(dispatcher.dispatch(command)))));
    }

    @PostMapping(value = "/compensate", version = "1+")
    @Operation(summary = "جبران‌سازی مرحله تصویب")
    public ResponseEntity<BaseResponse<EventStream>> compensateApproval(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @RequestBody @Valid CompensationRequest request) {

        var command = CompensateApprovalCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .loanFacilityId(facilityId)
                .build();

        return ResponseEntity.ok(BaseResponse.success(EventStream.of(unwrap(dispatcher.dispatch(command)))));
    }
}
