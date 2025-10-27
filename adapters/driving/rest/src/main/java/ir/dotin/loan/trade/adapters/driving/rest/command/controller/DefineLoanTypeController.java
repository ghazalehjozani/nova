package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.adapter.rest.request.DataRequest;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineLoanTypeRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.DefineLoanTypeRequestToCommandMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/loan-types/define")
@Tag(name = "عملیات مدیریت نوع تسهیلات", description = "عملیات مربوط به مدیریت نوع تسهیلات")
@RequiredArgsConstructor
public class DefineLoanTypeController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final DefineLoanTypeRequestToCommandMapper mapper;

    @PostMapping
    @Operation(
            summary = "ایجاد نوع تسهیلات",
            description = "در این عملیات نوع تسهیلات با توجه به اطلاعات وارد شده ساخته می شود.")
    public EventStreamResponse defineLoanType(
            @Parameter(description = "جزئیات ایجاد نوع تسهیلات", required = true) @RequestBody
                    DataRequest<DefineLoanTypeRequest> request) {
        var command = mapper.toCommand(request.payload());
        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
