package ir.dotin.loan.trade.core.domain.loantype.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

import static java.util.UUID.randomUUID;

public record TradeLoanTypeId(UUID value) implements Identity {

    public static TradeLoanTypeId generate() {
        return new TradeLoanTypeId(randomUUID());
    }

    public static TradeLoanTypeId from(UUID value) {
        return new TradeLoanTypeId(value);
    }
}
