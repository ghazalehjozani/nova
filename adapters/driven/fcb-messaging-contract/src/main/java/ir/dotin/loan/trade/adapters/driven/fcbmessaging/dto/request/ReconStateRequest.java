package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public final class ReconStateRequest extends FcbBaseRequest {

    @Builder.Default
    @JsonIgnore
    private String operationName = "nova-loanfile-recon-state";

    private final String facilityId;

    /**
     * Forward event uids (Nova outbox {@code eventId}s) FCB should return durable peer signals for;
     * {@code null}/omitted for a state-only probe (LN-59513).
     */
    private final @Nullable List<String> forwardEventUids;
}
