package ir.dotin.loan.trade.adapters.driven.persistence.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

/**
 * Shared MapStruct conversions between JPA audit timestamps and the query-side temporal type.
 *
 * <p>Persistence entities inherit {@code LocalDateTime} audit fields (createdAt / modifiedAt) from the platform
 * {@code PersistentEntity} base, while query DTOs expose {@link Instant} (SWA-101 UTC/{@code Z} contract). MapStruct
 * cannot bridge {@code LocalDateTime} and {@code Instant} on its own because the zone is ambiguous; this component
 * supplies the conversion using {@link ZoneOffset#UTC}, matching the UTC convention already used elsewhere in the
 * persistence adapter (see {@code JpaFacilityReconReadAdapter}).
 *
 * <p>Wired into every mapper via {@link BaseMapperConfig}'s {@code uses}. MapStruct guards the call with a null check
 * (see {@code NullValueCheckStrategy.ALWAYS}), so this method only ever receives a non-null value.
 */
@Component
public class TemporalMapper {

    public Instant toInstant(LocalDateTime value) {
        return value.toInstant(ZoneOffset.UTC);
    }
}
