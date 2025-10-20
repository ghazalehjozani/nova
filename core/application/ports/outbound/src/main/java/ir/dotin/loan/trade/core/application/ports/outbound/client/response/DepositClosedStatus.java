package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import org.jspecify.annotations.NonNull;

public record DepositClosedStatus(boolean isClosed, @NonNull String currencyTypeCode) {}
