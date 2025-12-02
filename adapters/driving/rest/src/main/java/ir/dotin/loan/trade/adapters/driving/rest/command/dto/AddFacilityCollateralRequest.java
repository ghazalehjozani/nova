package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AddFacilityCollateralRequest", description = "عملیات مدیریت وثایق تسهیلات")
public record AddFacilityCollateralRequest(
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        Integer version) {}
