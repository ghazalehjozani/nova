package ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject;

import ir.dotin.platform.ddd.common.entity.Identity;
import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import java.util.UUID;

public record MorabeheLoanApplicationId(UUID id) implements Identity {

    public static MorabeheLoanApplicationId generate() {
        return new MorabeheLoanApplicationId(TimeBasedUUIDGenerator.generate());
    }

}
