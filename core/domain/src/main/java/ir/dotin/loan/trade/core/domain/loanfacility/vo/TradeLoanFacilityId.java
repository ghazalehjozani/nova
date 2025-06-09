package ir.dotin.loan.trade.core.domain.loanfacility.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

import static java.util.UUID.randomUUID;

public record TradeLoanFacilityId(UUID value) implements Identity {

    public static TradeLoanFacilityId generate() {
        return new TradeLoanFacilityId(randomUUID());
    }
}
