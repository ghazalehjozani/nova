package ir.dotin.loan.trade.core.application.query.shared.pagination;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CursorPosition(@NotNull LocalDateTime timestamp, @NotNull UUID id) {
    @JsonCreator
    public CursorPosition(@JsonProperty("timestamp") LocalDateTime timestamp, @JsonProperty("id") UUID id) {
        this.timestamp = timestamp;
        this.id = id;
    }

    public static CursorPosition of(LocalDateTime timestamp, UUID id) {
        return new CursorPosition(timestamp, id);
    }
}
