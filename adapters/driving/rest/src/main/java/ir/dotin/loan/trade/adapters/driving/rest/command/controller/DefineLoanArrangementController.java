package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.protocol.api.response.BaseResponse;
import ir.dotin.platform.protocol.api.response.EventStream;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.DefineTradeLoanArrangementRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/loan-arrangements/define")
@Tag(name = SwaggerConfig.TAG_LOAN_ARRANGEMENT_MANAGEMENT, description = "عملیات مربوط به مدیریت شرایط اعطا")
@RequiredArgsConstructor
class DefineLoanArrangementController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final DefineTradeLoanArrangementRequestToCommandMapper mapper;

    @PostMapping(version = "1+")
    @Operation(summary = "ایجاد شرط اعطا")
    public ResponseEntity<BaseResponse<EventStream>> defineLoanArrangement(
            @RequestBody @Valid DefineTradeLoanArrangementRequest request) {
        var command =
                mapper.toCommand(request).toBuilder().uid(getIdempotencyKey()).build();
        return ResponseEntity.ok(BaseResponse.success(EventStream.of(unwrap(dispatcher.dispatch(command)))));
    }
}
