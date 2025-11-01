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
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.SubmitFacilityForApprovalRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.SubmitFacilityForApprovalRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/submit-for-approval")
@Tag(name = SwaggerConfig.TAG_FACILITY_APPROVAL_SUBMISSION, description = "عملیات مربوط به ثبت درخواست تصویب مصوبه")
@RequiredArgsConstructor
public class SubmitFacilityForApprovalController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final SubmitFacilityForApprovalRequestToCommandMapper mapper;

    @PostMapping
    @Operation(summary = "ثبت درخواست تصویب تسهیلات")
    public EventStreamResponse submitFacilityForApproval(
            @PathVariable UUID facilityId,
            @Parameter(description = "جزئیات ثبت درخواست تصویب مصوبه", required = true) @RequestBody @Valid
                    DataRequest<SubmitFacilityForApprovalRequest> request) {
        var command = mapper.toCommand(facilityId, request.payload());
        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
