package ir.dotin.loan.morabehe.core.domain.config.valueobject;

import ir.dotin.platform.ddd.common.entity.Identity;
import ir.dotin.platform.ddd.common.entity.TimeBasedUUIDGenerator;
import java.util.UUID;

public record MorabeheLoanTypeId(UUID id)  implements Identity {

    public static MorabeheLoanTypeId generate() {
        return new MorabeheLoanTypeId(TimeBasedUUIDGenerator.generate());
    }

}
