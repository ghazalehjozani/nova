package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import jakarta.validation.constraints.NotNull;

public record FullLifecycleRevertRequest(
        @NotNull Long version,
        String reason,
        String contractTransactionNumberToReverse,
        String disbursementTransactionNumberToReverse) {}
