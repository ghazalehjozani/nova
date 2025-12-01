package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.adapter.rest.request.DataRequest;
import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.commons.security.AuthenticationContextHolder;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.dispatcher.core.context.StandardHeaders;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.IssueFacilityContractRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/facilities/{facilityId}/issue-contract")
@Tag(name = SwaggerConfig.TAG_FACILITY_CONTRACT_ISSUANCE, description = "عملیات مربوط به صدور قرارداد تسهیلات")
@RequiredArgsConstructor
class IssueFacilityContractController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final AuthenticationContextHolder authenticationContextHolder;

    @PostMapping(version = "1+")
    @Operation(summary = "صدور قرارداد")
    public EventStreamResponse issueFacilityContract(
            @Parameter(
                            description = "شناسه یکتای تسهیلات جهت صدور قرارداد",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "جزئیات صدور قرارداد تسهیلات", required = true) @RequestBody
                    DataRequest<IssueFacilityContractRequest> request,
            HttpServletRequest httpServletRequest) {

        String branchCode = authenticationContextHolder
                .branchCode()
                .orElseThrow(() -> new IllegalStateException("Branch code missing in security context"));

        String userId = authenticationContextHolder.userId().orElse("SYSTEM");

        String ip = authenticationContextHolder.ipAddress().orElse("0.0.0.0");
        String requestId = httpServletRequest.getHeader(StandardHeaders.X_REQUEST_ID.toString());
        var metadata = request.metadata();
        // TODO: change metadata structure and remove default value
        var command = IssueFacilityContractCommand.builder()
                .id(UUID.fromString(requestId))
                .version(request.payload().version())
                .loanFacilityId(facilityId)
                .branchCode(branchCode)
                .userId(userId)
                .terminalIp(ip)
                .terminalId(metadata.getOrDefault("terminalId", "UNKNOWN"))
                .terminalType(metadata.getOrDefault("terminalType", "WEB"))
                .channel(metadata.getOrDefault("channel", "INTERNET_BANK"))
                .toolSource(metadata.getOrDefault("toolSource", "CORE"))
                .productCode(metadata.getOrDefault("productCode", "DEFAULT_PRODUCT"))
                .networkType(metadata.getOrDefault("networkType", "INTERNET"))
                .build();

        return EventStreamResponse.of(unwrap(dispatcher.dispatch(command)));
    }
}
