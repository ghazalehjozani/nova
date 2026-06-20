package ir.dotin.loan.trade.adapters.driving.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BindingRequest", description = "تعریف اتصال متغیر فرمول")
public record BindingRequest(
        @Schema(description = "نام متغیر", requiredMode = Schema.RequiredMode.REQUIRED)
        String variable,

        @Schema(description = "نوع اتصال", requiredMode = Schema.RequiredMode.REQUIRED)
        String type,

        @Schema(description = "مقدار ثابت") String literalValue,

        @Schema(description = "نام فیلد منبع") String fieldName,

        @Schema(description = "کد فرمول ارجاع‌شده") String referencedFormulaId,

        @Schema(description = "نام مقدار محاسبه‌شده") String computedName) {}
