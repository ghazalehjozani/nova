package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApproveFacilityRequest", description = "عملیات تصویب مصوبه")
public record ApproveFacilityRequest(
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                Long version) {}
