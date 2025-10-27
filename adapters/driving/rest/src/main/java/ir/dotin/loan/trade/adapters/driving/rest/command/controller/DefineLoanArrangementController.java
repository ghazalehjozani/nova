package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.adapter.rest.request.DataRequest;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.DefineTradeLoanArrangementRequestToCommandMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/loan-arrangements/define")
@Tag(name = "عملیات مدیریت شرایط اعطا", description = "عملیات مربوط به مدیریت شرایط اعطا")
@RequiredArgsConstructor
public class DefineLoanArrangementController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final DefineTradeLoanArrangementRequestToCommandMapper mapper;

    @PostMapping
    @Operation(
            summary = "افزودن شرط اعطا",
            description = "در این عملیات شرایط اعطا با توجه به اطلاعات وارد شده ساخته می شود.")
    public EventStreamResponse defineLoanArrangement(
            @Parameter(description = "جزئیات ایجاد شرط اعطا", required = true) @RequestBody
                    DataRequest<DefineTradeLoanArrangementRequest> request) {
        var command = mapper.toCommand(request.payload());
        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
