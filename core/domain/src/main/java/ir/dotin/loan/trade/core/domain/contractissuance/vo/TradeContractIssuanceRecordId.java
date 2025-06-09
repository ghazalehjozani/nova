package ir.dotin.loan.trade.core.domain.contractissuance.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

import static java.util.UUID.randomUUID;

public record TradeContractIssuanceRecordId(UUID value) implements Identity {

    public static TradeContractIssuanceRecordId of(UUID value) {
        return new TradeContractIssuanceRecordId(value);
    }

    public static TradeContractIssuanceRecordId generate() {
        return new TradeContractIssuanceRecordId(randomUUID());
    }
}
