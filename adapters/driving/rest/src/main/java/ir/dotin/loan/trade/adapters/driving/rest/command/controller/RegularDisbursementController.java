package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.adapter.rest.request.DataRequest;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.RegularDisbursementRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.RegularDisbursementRequestToCommandMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/disburse/regular")
@Tag(name = "عملیات پرداخت عادی", description = "عملیات مربوط به پرداخت عادی تسهیلات")
@RequiredArgsConstructor
public class RegularDisbursementController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final RegularDisbursementRequestToCommandMapper mapper;

    @PostMapping
    @Operation(
            summary = "پرداخت عادی تسهیلات",
            description = "این عملیات پرداخت را طبق برنامه زمانی عادی و با مبلغ مشخص انجام می‌دهد.")
    public EventStreamResponse regularDisbursement(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات درخواست پرداخت عادی", required = true) @RequestBody
                    DataRequest<RegularDisbursementRequest> requestBody) {
        var command = mapper.toCommand(facilityId, requestBody.payload());
        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
