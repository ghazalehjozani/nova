package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.util.List;
import jakarta.validation.constraints.NotNull;

public record FullLifecycleRevertRequest(
        @NotNull Long version,
        String reason,
        String contractTransactionNumberToReverse,
        String disbursementTransactionNumberToReverse,
        List<String> collateralSerialsToRevert) {}
