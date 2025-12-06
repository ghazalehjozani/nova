package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LumpSumDisbursementRequest", description = "درخواست پرداخت یکجای تسهیلات")
public record LumpSumDisbursementRequest(
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        Long version,

        @Schema(description = "تاریخ پرداخت") LocalDate disbursementDate) {}
