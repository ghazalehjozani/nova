package ir.dotin.loan.morabehe.core.domain.contractissuance.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record MorabeheContractIssuanceRecordId(UUID value) implements Identity {

    public static MorabeheContractIssuanceRecordId of(UUID value) {
        return new MorabeheContractIssuanceRecordId(value);
    }

    public static MorabeheContractIssuanceRecordId generate() {
        return new MorabeheContractIssuanceRecordId(UUID.randomUUID());
    }
}
