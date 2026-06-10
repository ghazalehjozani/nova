package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DefineLoanTypeRequest;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.DefineLoanTypeRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-types")
@Tag(name = SwaggerConfig.TAG_LOAN_TYPE_MANAGEMENT, description = "عملیات مربوط به مدیریت نوع تسهیلات")
@RequiredArgsConstructor
class DefineLoanTypeController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final DefineLoanTypeRequestToCommandMapper mapper;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "ایجاد نوع تسهیلات")
    public ResponseEntity<Void> defineLoanType(@RequestBody @Valid DefineLoanTypeRequest request) {
        var command =
                mapper.toCommand(request).toBuilder().uid(getIdempotencyKey()).build();
        var result = dispatcher.dispatch(command);
        return responseFactory.created(result, "loan-types");
    }
}
