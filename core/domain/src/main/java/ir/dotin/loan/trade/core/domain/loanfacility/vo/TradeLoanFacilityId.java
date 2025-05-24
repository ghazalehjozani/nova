package ir.dotin.loan.trade.core.domain.loanfacility.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record TradeLoanFacilityId(UUID value) implements Identity {

    public static TradeLoanFacilityId generate() {
        return new TradeLoanFacilityId(UUID.randomUUID());
    }
}
