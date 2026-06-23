package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.contract.dto.AddGuarantorsRequest;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.AddGuarantorsRequestToCommandMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddGuarantorsCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RemoveGuarantorCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-facilities/{facilityId}/guarantors")
@Tag(name = SwaggerConfig.TAG_FACILITY_GUARANTOR_MANAGEMENT, description = "عملیات مدیریت ضامنین تسهیلات")
@RequiredArgsConstructor
class GuarantorManagementController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final AddGuarantorsRequestToCommandMapper addMapper;
    private final AuthenticationContextHolder authenticationContextHolder;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "افزودن ضامن/ضامنین تسهیلات")
    public ResponseEntity<Void> addGuarantors(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "فهرست ضامنین برای افزودن", required = true) @RequestBody @Valid
                    AddGuarantorsRequest request) {

        AddGuarantorsCommand command = addMapper.toCommand(facilityId, request).toBuilder()
                .uid(getIdempotencyKey())
                .branchCode(authenticationContextHolder.branchCode().orElseThrow())
                .build();
        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }

    @DeleteMapping(value = "/{customerNumber}", version = "1+")
    @Operation(summary = "حذف ضامن تسهیلات")
    public ResponseEntity<Void> removeGuarantor(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "شماره مشتری ضامن", example = "67890", required = true) @PathVariable
                    String customerNumber,
            @Parameter(description = "نسخه عملیات (قفل خوش‌بینانه)", example = "1", required = true) @RequestParam
                    Long version) {

        RemoveGuarantorCommand command = RemoveGuarantorCommand.builder()
                .uid(getIdempotencyKey())
                .version(version)
                .loanFacilityId(facilityId)
                .customerNumber(customerNumber)
                .branchCode(authenticationContextHolder.branchCode().orElseThrow())
                .build();
        var result = dispatcher.dispatch(command);
        return responseFactory.mutated(result);
    }
}
