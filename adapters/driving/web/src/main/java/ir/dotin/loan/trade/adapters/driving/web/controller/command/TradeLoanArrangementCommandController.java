package ir.dotin.loan.trade.adapters.driving.web.controller.command;

import jakarta.validation.Valid;

import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.web.controller.command.dto.BaseResponse;
import ir.dotin.loan.trade.adapters.driving.web.controller.command.dto.EstablishArrangementRequest;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/trade/loan-arrangements")
@Tag(name = "Trade Loan Arrangement Commands", description = "Command operations for Trade Loan Arrangement management")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class TradeLoanArrangementCommandController {

    private final CommandDispatcher commandDispatcher;
    private final ConversionService mapper;

    public TradeLoanArrangementCommandController(CommandDispatcher commandDispatcher, ConversionService mapper) {
        this.commandDispatcher = commandDispatcher;
        this.mapper = mapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Establish new trade loan arrangement",
            description = "Creates a new trade loan arrangement with specified configuration and policies")
    public ResponseEntity<BaseResponse> establish(@Valid @RequestBody EstablishArrangementRequest request) {
        var command = mapper.convert(request, EstablishTradeLoanArrangementCommand.class);
        var events = commandDispatcher.dispatch(command);
        var response = BaseResponse.builder().events(events).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
