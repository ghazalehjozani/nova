package ir.dotin.loan.trade.adapters.driving.rest.base.response;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import ir.dotin.platform.commons.domain.event.DomainEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventStream(List<DomainEvent<?, ?>> events, int eventCount, UUID firstEventId, UUID lastEventId) {

    public EventStream(List<DomainEvent<?, ?>> events) {
        this(
                events == null ? Collections.emptyList() : List.copyOf(events),
                events == null ? 0 : events.size(),
                events != null && !events.isEmpty() ? events.getFirst().eventId() : null,
                events != null && !events.isEmpty() ? events.getLast().eventId() : null);
    }

    public static EventStream empty() {
        return new EventStream(Collections.emptyList());
    }

    public static EventStream of(List<DomainEvent<?, ?>> events) {
        return new EventStream(events);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return events.isEmpty();
    }

    @JsonIgnore
    public boolean hasEvents() {
        return !events.isEmpty();
    }

    @JsonIgnore
    public EventStream filter(Predicate<DomainEvent<?, ?>> predicate) {
        List<DomainEvent<?, ?>> filtered = events.stream().filter(predicate).toList();
        return new EventStream(filtered);
    }
}
