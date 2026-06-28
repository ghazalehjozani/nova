package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateFacilityCollateralRequest", description = "عملیات ویرایش وثایق تسهیلات")
public record UpdateFacilityCollateralRequest(
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer version,

        @Schema(description = "مجموعه کامل وثایق پس از ویرایش", requiredMode = Schema.RequiredMode.REQUIRED)
        List<CollateralDto> collaterals,

        Map<String, String> metadata)
        implements BaseRequest {

    @Schema(description = "Collateral details")
    public record CollateralDto(
            @Schema(description = "Collateral type code", example = "ESTATE", required = true)
            String collateralTypeCode,

            @Schema(description = "Collateral description", example = "Property deed", required = true)
            String description,

            @Schema(description = "Collateral serial number", example = "COLL-2025-001", required = true)
            String collateralSerial,

            @Schema(description = "Used amount from collateral", required = true)
            MoneyDto usedAmount) {}

    public record MoneyDto(
            @Schema(description = "Amount", example = "1000000", required = true)
            BigDecimal value,

            @Schema(description = "Currency code", example = "IRR", required = true)
            String currency) {}
}
