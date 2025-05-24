package ir.dotin.loan.trade.core.domain.loanarrangement.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record TradeLoanArrangementId(UUID value) implements Identity {

    public static TradeLoanArrangementId generate() {
        return new TradeLoanArrangementId(UUID.randomUUID());
    }
}
