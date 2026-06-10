package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.Map;
import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CompensationRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.IssueFacilityContractRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateContractIssuanceCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-facilities/{facilityId}/issue-contract")
@Tag(name = SwaggerConfig.TAG_FACILITY_CONTRACT_ISSUANCE, description = "عملیات مربوط به صدور قرارداد تسهیلات")
@RequiredArgsConstructor
class IssueFacilityContractController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final AuthenticationContextHolder authenticationContextHolder;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "صدور قرارداد")
    public ResponseEntity<Void> issueFacilityContract(
            @Parameter(
                            description = "شناسه یکتای تسهیلات جهت صدور قرارداد",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات صدور قرارداد تسهیلات", required = true) @RequestBody
                    IssueFacilityContractRequest request) {

        Map<String, String> metadata = request.metadata() == null ? Map.of() : request.metadata();
        var command = IssueFacilityContractCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .loanFacilityId(facilityId)
                .branchCode(authenticationContextHolder.branchCode().orElseThrow())
                .userId(authenticationContextHolder.userIdOrThrow())
                .terminalIp(authenticationContextHolder.ipAddress().orElseThrow())
                .terminalId(metadata.get("terminalId"))
                .terminalType(metadata.get("terminalType"))
                .channel(metadata.get("channel"))
                .toolSource(metadata.get("toolSource"))
                .productCode(metadata.get("productCode"))
                .networkType(metadata.get("networkType"))
                .build();

        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }

    @PostMapping(value = "/compensate", version = "1+")
    @Operation(summary = "جبران‌سازی مرحله صدور قرارداد")
    public ResponseEntity<Void> compensateContractIssuance(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @RequestBody @Valid CompensationRequest request) {

        var command = CompensateContractIssuanceCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .loanFacilityId(facilityId)
                .build();

        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }
}
