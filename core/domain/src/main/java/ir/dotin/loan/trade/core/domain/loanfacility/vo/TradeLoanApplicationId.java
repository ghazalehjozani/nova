package ir.dotin.loan.trade.core.domain.loanfacility.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record TradeLoanApplicationId(UUID value) implements Identity {
    public static TradeLoanApplicationId generate() {
        return new TradeLoanApplicationId(UUID.randomUUID());
    }
}
