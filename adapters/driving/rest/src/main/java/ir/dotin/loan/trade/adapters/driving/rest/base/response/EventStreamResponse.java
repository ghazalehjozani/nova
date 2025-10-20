package ir.dotin.loan.trade.adapters.driving.rest.base.response;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.trade.adapters.driving.rest.base.ServiceError;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventStreamResponse(String rsCode, Boolean isSuccess, EventStream data, List<ServiceError> errors)
        implements ServiceResponse<EventStream> {

    public EventStreamResponse {
        errors = errors == null ? null : List.copyOf(errors);
    }

    public static EventStreamResponse success(EventStream eventStream) {
        return new EventStreamResponse("0", true, eventStream, null);
    }

    public static EventStreamResponse success(List<DomainEvent<?, ?>> events) {
        return success(new EventStream(events));
    }

    public static EventStreamResponse empty() {
        return success(EventStream.empty());
    }

    public static EventStreamResponse failure(ServiceError error) {
        return new EventStreamResponse("1", false, null, List.of(error));
    }

    public static EventStreamResponse failure(List<ServiceError> errors) {
        return new EventStreamResponse("1", false, null, errors);
    }

    public static EventStreamResponse failure(String code, String message) {
        return failure(ServiceError.of(code, message));
    }

    @JsonIgnore
    public EventStreamResponse filterEvents(Predicate<DomainEvent<?, ?>> predicate) {
        if (hasErrors() || data == null) {
            return this;
        }
        return success(data.filter(predicate));
    }

    @Override
    public <R> ServiceResponse<R> map(Function<? super EventStream, ? extends R> mapper) {
        if (hasErrors()) {
            return new DataResponse<>(rsCode, isSuccess, null, errors);
        }
        return data == null
                ? new DataResponse<>(rsCode, isSuccess, null, null)
                : DataResponse.success(mapper.apply(data));
    }

    @Override
    public <R> ServiceResponse<R> flatMap(Function<? super EventStream, ? extends ServiceResponse<R>> mapper) {
        if (hasErrors()) {
            return new DataResponse<>(rsCode, isSuccess, null, errors);
        }
        return data == null ? new DataResponse<>(rsCode, isSuccess, null, null) : mapper.apply(data);
    }
}
