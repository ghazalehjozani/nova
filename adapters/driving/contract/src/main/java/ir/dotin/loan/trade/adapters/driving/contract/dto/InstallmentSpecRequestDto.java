package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

import ir.dotin.loan.trade.adapters.driving.contract.dto.validation.Money;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InstallmentSpecDto", description = "مشخصات قسط")
public record InstallmentSpecRequestDto(
        @Schema(description = "شماره ترتیب قسط", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        Integer sequenceNumber,

        @Schema(description = "سررسید قسط", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        LocalDate dueDate,

        @Schema(description = "مبلغ اصل", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Money
        BigDecimal principalAmount,

        @Schema(description = "مبلغ سود", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Money
        BigDecimal interestAmount,

        @Schema(description = "مبلغ جریمه", requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Money
        BigDecimal penaltyAmount,

        @Schema(description = "مبلغ کارمزد", requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Money
        BigDecimal feeAmount) {}
