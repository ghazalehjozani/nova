package ir.dotin.loan.morabehe.core.domain.loanfacility.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record MorabeheLoanApplicationId(UUID value) implements Identity {
    public static MorabeheLoanApplicationId generate() {
        return new MorabeheLoanApplicationId(UUID.randomUUID());
    }
}
