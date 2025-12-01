package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.adapter.rest.request.DataRequest;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.RegularDisbursementRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/facilities/{facilityId}/disburse/regular")
@Tag(name = SwaggerConfig.TAG_REGULAR_DISBURSEMENT, description = "عملیات مربوط به پرداخت عادی تسهیلات")
@RequiredArgsConstructor
@Hidden
class RegularDisbursementController extends BaseController {

    private final CommandDispatcher dispatcher;

    @PostMapping(version = "1+")
    @Operation(summary = "پرداخت عادی")
    public EventStreamResponse regularDisbursement(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات درخواست پرداخت عادی", required = true) @RequestBody
                    DataRequest<RegularDisbursementRequest> requestBody) {
        throw new UnsupportedOperationException("No impl");
    }
}
