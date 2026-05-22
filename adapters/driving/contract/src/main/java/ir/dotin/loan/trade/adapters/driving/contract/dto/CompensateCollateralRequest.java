package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

public record CompensateCollateralRequest(
        @NotNull Integer version, List<String> collateralSerials, Map<String, String> metadata)
        implements BaseRequest {}
