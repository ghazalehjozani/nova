package ir.dotin.loan.trade.core.domain.disbursement.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record TradeDisbursementRecordId(UUID value) implements Identity {

    public static TradeDisbursementRecordId of(UUID value) {
        return new TradeDisbursementRecordId(value);
    }

    public static TradeDisbursementRecordId generate() {
        return new TradeDisbursementRecordId(UUID.randomUUID());
    }
}
