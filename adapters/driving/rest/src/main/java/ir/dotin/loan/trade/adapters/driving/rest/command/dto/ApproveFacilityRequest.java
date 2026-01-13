package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.util.Map;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApproveFacilityRequest", description = "عملیات تصویب مصوبه")
public record ApproveFacilityRequest(
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        Long version,

        @Schema(description = "مرجع تصویب", example = "99990000", requiredMode = Schema.RequiredMode.REQUIRED)
        String confirmType,

        Map<String, String> metadata)
        implements BaseRequest {}
