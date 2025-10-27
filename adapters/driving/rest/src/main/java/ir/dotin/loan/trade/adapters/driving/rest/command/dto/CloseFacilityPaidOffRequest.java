package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CloseFacilityPaidOffRequest", description = "عملیات بستن تسهیلات پرداخت شده")
public record CloseFacilityPaidOffRequest(
        @Schema(
                        description = "شناسه عملیات",
                        example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                UUID uid,
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                Integer version) {}
