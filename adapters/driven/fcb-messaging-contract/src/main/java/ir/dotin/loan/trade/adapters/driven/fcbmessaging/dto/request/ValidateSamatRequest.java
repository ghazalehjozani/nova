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
public final class ValidateSamatRequest extends FcbKafkaBaseRequest {

    @Builder.Default
    private String operationName = "validate-samat";

    private final String trackingNumber;
    private final String isicEconomicSector;
    private final String subIsicEconomicSector;
    private final String useType;
    private final String exceptionCode;
    private final String consumptionPlaceCode;
    private final String loanTypeCode;
    private final String economicalSectionCode;
}
