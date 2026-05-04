package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

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
public final class LoadCustomerInfoRequest extends FcbKafkaBaseRequest {

    @Builder.Default
    private String operationName = "load-customer-info";

    private final String customerNumber;
    private final String sequenceCode;
    private final String subsystem;
    private final boolean includeCapability;
    private final boolean includeBlackList;
    private final boolean includeBaseInfo;
    private final boolean includeGrayList;
}
