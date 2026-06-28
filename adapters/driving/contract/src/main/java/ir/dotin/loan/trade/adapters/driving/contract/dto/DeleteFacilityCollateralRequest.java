package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.List;
import java.util.Map;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DeleteFacilityCollateralRequest", description = "عملیات حذف وثایق تسهیلات")
public record DeleteFacilityCollateralRequest(
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer version,

        @Schema(description = "شماره سریال وثایق برای حذف", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> collateralSerials,

        Map<String, String> metadata)
        implements BaseRequest {}
