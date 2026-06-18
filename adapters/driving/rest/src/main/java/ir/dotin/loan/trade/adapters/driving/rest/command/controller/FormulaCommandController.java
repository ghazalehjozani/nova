package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.net.URI;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.formula.service.cqrs.command.CreateFormulaCommand;
import ir.dotin.platform.formula.service.cqrs.command.DeleteFormulaCommand;
import ir.dotin.platform.formula.service.cqrs.command.UpdateFormulaCommand;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CreateFormulaRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.UpdateFormulaRequest;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.CreateFormulaRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.UpdateFormulaRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/formulas")
@Tag(name = SwaggerConfig.TAG_FORMULAS_COMMANDS, description = "عملیات مدیریت فرمول‌ها")
@RequiredArgsConstructor
class FormulaCommandController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final CreateFormulaRequestToCommandMapper createMapper;
    private final UpdateFormulaRequestToCommandMapper updateMapper;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "ایجاد فرمول")
    public ResponseEntity<Void> createFormula(@RequestBody @Valid CreateFormulaRequest request) {
        CreateFormulaCommand command = createMapper.toCommand(request, getIdempotencyKey());
        dispatcher.dispatch(command);
        return ResponseEntity.created(URI.create("/v1/formulas/" + command.code())).build();
    }

    @PutMapping(value = "/{code}", version = "1+")
    @Operation(summary = "ویرایش فرمول")
    public ResponseEntity<Void> updateFormula(
            @PathVariable String code, @RequestBody @Valid UpdateFormulaRequest request) {
        UpdateFormulaCommand command = updateMapper.toCommand(code, request, getIdempotencyKey());
        return responseFactory.mutated(dispatcher.dispatch(command));
    }

    @DeleteMapping(value = "/{code}", version = "1+")
    @Operation(summary = "حذف فرمول")
    public ResponseEntity<Void> deleteFormula(
            @PathVariable String code, @RequestParam(defaultValue = "false") boolean cascade) {
        DeleteFormulaCommand command = new DeleteFormulaCommand(getIdempotencyKey(), null, code, cascade);
        return responseFactory.mutated(dispatcher.dispatch(command));
    }
}
