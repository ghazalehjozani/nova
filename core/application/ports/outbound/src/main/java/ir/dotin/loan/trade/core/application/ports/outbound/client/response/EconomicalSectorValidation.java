package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import org.jspecify.annotations.Nullable;

public record EconomicalSectorValidation(
        boolean isValid, @Nullable String message) {}
