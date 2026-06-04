package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public final class UnReserveCollateralRequest extends FcbKafkaBaseRequest {

    @Builder.Default
    @JsonIgnore
    private String operationName = "un-reserve-assurance-for-file";

    private final String assuranceSerial;
    private final String fileNumber;
    private final String transactionId;
    private final String rollBackId;
}
