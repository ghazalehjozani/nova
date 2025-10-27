package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

import org.springframework.web.bind.annotation.*;

import ir.dotin.platform.adapter.rest.response.EventStreamResponse;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.AddFacilityCollateralRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.mapper.AddFacilityCollateralRequestToCommandMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/facilities/{facilityId}/collateral")
@Tag(name = "عملیات مدیریت وثایق تسهیلات", description = "عملیات مربوط به مدیریت وثایق تسهیلات")
@RequiredArgsConstructor
public class AddFacilityCollateralController {

    private final CommandDispatcher dispatcher;
    private final AddFacilityCollateralRequestToCommandMapper mapper;

    @PostMapping("/{collateralSerial}")
    @Operation(
            summary = "افزودن وثیقه به تسهیلات",
            description = "این عملیات شماره سریال وثیقه را با تسهیلات موجود مرتبط می‌کند.")
    public EventStreamResponse addCollateral(
            @Parameter(
                            description = "شناسه یکتای تسهیلات",
                            example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                            required = true)
                    @PathVariable
                    UUID facilityId,
            @Parameter(description = "شماره سریال یکتای وثیقه جهت افزودن", example = "COLL-2025-001", required = true)
                    @NotBlank
                    @PathVariable
                    String collateralSerial,
            @Parameter(description = "جزئیات افزودن وثیقه به تسهیلات", required = true) @RequestBody
                    AddFacilityCollateralRequest request) {
        var command = mapper.toCommand(facilityId, collateralSerial, request);
        List<DomainEvent<?, ?>> domainEvents = dispatcher.dispatch(command);
        return EventStreamResponse.of(domainEvents);
    }
}
