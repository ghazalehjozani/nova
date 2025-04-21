package ir.dotin.loan.morabehe.core.domain.disbursement.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record MorabeheDisbursementRecordId(UUID value) implements Identity {

    public static MorabeheDisbursementRecordId of(UUID value) {
        return new MorabeheDisbursementRecordId(value);
    }

    public static MorabeheDisbursementRecordId generate() {
        return new MorabeheDisbursementRecordId(UUID.randomUUID());
    }
}
