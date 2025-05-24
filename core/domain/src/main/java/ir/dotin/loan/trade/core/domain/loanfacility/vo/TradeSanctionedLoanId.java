package ir.dotin.loan.trade.core.domain.loanfacility.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record TradeSanctionedLoanId(UUID value) implements Identity {
    public static TradeSanctionedLoanId generate() {
        return new TradeSanctionedLoanId(UUID.randomUUID());
    }
}
