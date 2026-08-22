package ir.dotin.loan.trade.adapters.driving.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SamatDto", description = "اطلاعات مربوط به سمات")
public record SamatRequestDto(
        @Schema(description = "شماره پیگیری سمات", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String trackingNumber,

        @Schema(description = "بخش اقتصادی isic", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String isicEconomicSector,

        @Schema(description = "زیر بخش اقتصادی isic", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String subIsicEconomicSector,

        @Schema(description = "نوع استفاده", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String useType,

        @Schema(description = "کد استثنا", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String exceptionCode,

        @Schema(description = "شهر محل مصرف", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String consumptionPlaceCode) {}
