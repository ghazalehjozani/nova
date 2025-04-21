package ir.dotin.loan.morabehe.core.domain.loanfacility.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record MorabeheLoanFacilityId(UUID value) implements Identity {

    public static MorabeheLoanFacilityId generate() {
        return new MorabeheLoanFacilityId(UUID.randomUUID());
    }
}
