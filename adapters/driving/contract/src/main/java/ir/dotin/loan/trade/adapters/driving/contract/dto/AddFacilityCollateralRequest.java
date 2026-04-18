package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ir.dotin.platform.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AddFacilityCollateralRequest", description = "عملیات مدیریت وثایق تسهیلات")
public record AddFacilityCollateralRequest(
        @Schema(
                description = "شناسه عملیات",
                example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID uid,

        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer version,

        @Schema(description = "اطلاعات وثایق", requiredMode = Schema.RequiredMode.REQUIRED)
        List<CollateralDto> collaterals,

        Map<String, String> metadata)
        implements BaseRequest {

    @Schema(description = "Collateral details")
    public record CollateralDto(
            @Schema(description = "Collateral type code", example = "ESTATE", required = true)
            String collateralTypeCode,

            @Schema(description = "Collateral percentage", example = "100", required = true)
            Integer percent,

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
