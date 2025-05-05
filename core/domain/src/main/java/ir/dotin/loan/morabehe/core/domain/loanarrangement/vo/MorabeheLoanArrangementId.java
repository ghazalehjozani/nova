package ir.dotin.loan.morabehe.core.domain.loanarrangement.vo;

import java.util.UUID;

import ir.dotin.platform.domain.common.entity.Identity;

public record MorabeheLoanArrangementId(UUID value) implements Identity {

    public static MorabeheLoanArrangementId generate() {
        return new MorabeheLoanArrangementId(UUID.randomUUID());
    }
}
