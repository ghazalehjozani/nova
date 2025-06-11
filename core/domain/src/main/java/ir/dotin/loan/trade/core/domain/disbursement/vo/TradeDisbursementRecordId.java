package ir.dotin.loan.trade.core.domain.disbursement.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

import static java.util.UUID.randomUUID;

public record TradeDisbursementRecordId(UUID value) implements Identity {

    public static TradeDisbursementRecordId of(UUID value) {
        return new TradeDisbursementRecordId(value);
    }

    public static TradeDisbursementRecordId generate() {
        return new TradeDisbursementRecordId(randomUUID());
    }
}
