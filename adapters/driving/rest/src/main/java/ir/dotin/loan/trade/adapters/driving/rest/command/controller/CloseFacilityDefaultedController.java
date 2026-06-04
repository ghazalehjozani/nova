package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CloseFacilityDefaultedRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityDefaultedCommand;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-facilities/{facilityId}/close-defaulted")
@Tag(name = SwaggerConfig.TAG_FACILITY_CLOSURE_DEFAULTED, description = "عملیات مربوط به بستن تسهیلات معوق")
@RequiredArgsConstructor
class CloseFacilityDefaultedController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final AuthenticationContextHolder authenticationContextHolder;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "بستن تسهیلات معوق")
    @Hidden
    public ResponseEntity<Void> closeFacilityDefaulted(
            @Parameter(
                            description = "شناسه یکتای تسهیلات معوق جهت بستن",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات عملیات بستن تسهیلات", required = true) @RequestBody
                    CloseFacilityDefaultedRequest request) {
        // Idempotency/correlation key comes from the filter-populated InvocationContext (X-Correlation-ID /
        // Idempotency-Key), never from the request body, so it is consistent across every command controller.
        var command = CloseFacilityDefaultedCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version().longValue())
                .loanFacilityId(facilityId)
                .branchCode(authenticationContextHolder.branchCode().orElseThrow())
                .build();
        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }
}
