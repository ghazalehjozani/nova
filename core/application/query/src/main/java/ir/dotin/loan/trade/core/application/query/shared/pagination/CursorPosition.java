package ir.dotin.loan.trade.core.application.query.shared.pagination;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public record CursorPosition(@NotNull UUID id) {

    @JsonCreator
    public CursorPosition(@JsonProperty("id") UUID id) {
        this.id = id;
    }

    public static CursorPosition of(UUID id) {
        return new CursorPosition(id);
    }

    @JsonProperty("timestamp")
    @NotNull
    public LocalDateTime timestamp() {
        requireNonNull(id);
        long timestampMs = id.getMostSignificantBits() >>> 16;

        return Instant.ofEpochMilli(timestampMs).atZone(ZoneId.of("UTC")).toLocalDateTime();
    }
}
