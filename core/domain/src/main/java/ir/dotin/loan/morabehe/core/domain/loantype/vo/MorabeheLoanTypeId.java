package ir.dotin.loan.morabehe.core.domain.loantype.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record MorabeheLoanTypeId(UUID value) implements Identity {

    public static MorabeheLoanTypeId generate() {
        return new MorabeheLoanTypeId(UUID.randomUUID());
    }

    public static MorabeheLoanTypeId from(UUID value) {
        return new MorabeheLoanTypeId(value);
    }
}
