package ir.dotin.loan.core.domain.config.valueobject;

import ir.dotin.platform.ddd.common.entity.Identity;
import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import java.util.UUID;

public record MorabeheLoanRuleId(UUID id) implements Identity {

    public static MorabeheLoanRuleId generate() {
        return new MorabeheLoanRuleId(TimeBasedUUIDGenerator.generate());
    }

}
