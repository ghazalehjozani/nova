package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.loan.trade.core.application.ports.inbound.dto.MoneyDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "IrregularDisbursementRequest", description = "درخواست پرداخت نامنظم تسهیلات")
public record IrregularDisbursementRequest(
        @Schema(description = "مبلغ درخواستی پرداخت", example = "1000000", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                MoneyDto requestedAmount,
        @Schema(
                        description = "شناسه عملیات",
                        example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                UUID uid,
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                Integer version) {}
