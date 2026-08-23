package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.util.Map;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PlanSingleInstallmentScheduleRequest", description = "درخواست برنامه‌ریزی جدول اقساط تک‌قسطی")
public record PlanSingleInstallmentScheduleRequest(
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        Long version,

        @Schema(description = "مبلغ کل تسهیلات", example = "1000000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DecimalMin(value = "0")
        BigDecimal totalLoanAmount,

        @Schema(description = "کد ارز", example = "IRR", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String currency,

        @Schema(description = "نرخ سود", example = "18", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        BigDecimal interestRate,

        @Schema(description = "تعداد روزهای دوره تنفس", example = "30") Integer gracePeriodDays,

        Map<String, String> metadata)
        implements BaseRequest {}
