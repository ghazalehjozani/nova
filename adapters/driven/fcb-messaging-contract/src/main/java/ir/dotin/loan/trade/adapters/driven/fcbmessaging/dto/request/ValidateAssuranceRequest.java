package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

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
public final class ValidateAssuranceRequest extends FcbBaseRequest {

    @Builder.Default
    @JsonIgnore
    private String operationName = "validate-add-assurance-to-file";

    private final List<String> assuranceSerials;
    private final List<Long> usedCosts;
    private final List<String> branchCodes;
}
