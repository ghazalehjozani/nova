package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CancelFacilityRequest", description = "درخواست لغو تسهیلات")
public record CancelFacilityRequest(
        @Schema(
                        description = "یادداشت‌های لغو تسهیلات",
                        example = "لغو تسهیلات به دلیل عدم ارائه مدارک",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                @Size(max = 1000, message = "طول یادداشت‌های لغو نباید بیشتر از 1000 کاراکتر باشد.")
                String cancellationNotes,
        @Schema(
                        description = "شناسه عملیات",
                        example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                UUID uid,
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                Integer version) {}
