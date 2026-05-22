package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

public record FullLifecycleRevertRequest(
        @NotNull Long version,
        String reason,
        String contractTransactionNumberToReverse,
        String disbursementTransactionNumberToReverse,
        List<String> collateralSerialsToRevert,
        Map<String, String> metadata)
        implements BaseRequest {}
