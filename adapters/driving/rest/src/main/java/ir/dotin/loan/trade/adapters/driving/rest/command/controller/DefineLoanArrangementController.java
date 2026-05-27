package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DefineTradeLoanArrangementRequest;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.DefineTradeLoanArrangementRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-arrangements")
@Tag(name = SwaggerConfig.TAG_LOAN_ARRANGEMENT_MANAGEMENT, description = "عملیات مربوط به مدیریت شرایط اعطا")
@RequiredArgsConstructor
class DefineLoanArrangementController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final DefineTradeLoanArrangementRequestToCommandMapper mapper;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "ایجاد شرط اعطا")
    public ResponseEntity<Void> defineLoanArrangement(@RequestBody @Valid DefineTradeLoanArrangementRequest request) {
        var command =
                mapper.toCommand(request).toBuilder().uid(getIdempotencyKey()).build();
        var result = dispatcher.dispatch(command);
        return responseFactory.created(result, "loan-arrangements");
    }
}
