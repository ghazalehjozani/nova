package ir.dotin.loan.morabehe.core.domain.loanfacility.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record MorabeheSanctionedLoanId(UUID value) implements Identity {
    public static MorabeheSanctionedLoanId generate() {
        return new MorabeheSanctionedLoanId(UUID.randomUUID());
    }
}
