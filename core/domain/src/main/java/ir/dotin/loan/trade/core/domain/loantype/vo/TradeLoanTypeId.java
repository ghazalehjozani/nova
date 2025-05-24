package ir.dotin.loan.trade.core.domain.loantype.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record TradeLoanTypeId(UUID value) implements Identity {

    public static TradeLoanTypeId generate() {
        return new TradeLoanTypeId(UUID.randomUUID());
    }

    public static TradeLoanTypeId from(UUID value) {
        return new TradeLoanTypeId(value);
    }
}
