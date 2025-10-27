package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.List;
import java.util.UUID;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.IrregularDisbursementRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.IrregularDisbursementRequestToCommandMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/disburse/irregular")
@Tag(name = "عملیات پرداخت نامنظم", description = "عملیات مربوط به پرداخت نامنظم تسهیلات")
@RequiredArgsConstructor
public class IrregularDisbursementController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final IrregularDisbursementRequestToCommandMapper mapper;

    @PostMapping
    @Operation(
            summary = "پرداخت نامنظم تسهیلات",
            description = "این عملیات امکان پرداخت مبلغ متغیر خارج از برنامه عادی را فراهم می‌کند.")
    public EventStreamResponse irregularDisbursement(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات درخواست پرداخت نامنظم", required = true) @RequestBody
                    IrregularDisbursementRequest requestBody) {
        var command = mapper.toCommand(facilityId, requestBody);
        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
