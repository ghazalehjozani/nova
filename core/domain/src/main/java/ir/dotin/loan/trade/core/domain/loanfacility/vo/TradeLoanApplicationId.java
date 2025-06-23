package ir.dotin.loan.trade.core.domain.loanfacility.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanApplicationId(UUID value) implements Identity {
    public TradeLoanApplicationId {
        requireNonNull(value, "UUID cannot be null");
    }

    public static TradeLoanApplicationId generate() {
        return new TradeLoanApplicationId(randomUUID());
    }
}
