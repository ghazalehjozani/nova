package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import org.jspecify.annotations.Nullable;

public record CollateralValidation(
        boolean isValid, @Nullable String message) {}
