package ir.dotin.loan.trade.adapters.driving.messaging.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DisburseDestinationRequestDto.DepositDestinationDto.class, name = "DEPOSIT"),
    @JsonSubTypes.Type(value = DisburseDestinationRequestDto.AccountDestinationDto.class, name = "ACCOUNT")
})
public sealed interface DisburseDestinationRequestDto
        permits DisburseDestinationRequestDto.DepositDestinationDto,
                DisburseDestinationRequestDto.AccountDestinationDto {

    DisburseDestinationType type();

    record DepositDestinationDto(String depositNumber) implements DisburseDestinationRequestDto {
        @Override
        public DisburseDestinationType type() {
            return DisburseDestinationType.DEPOSIT;
        }
    }

    record AccountDestinationDto(String accountNumber) implements DisburseDestinationRequestDto {
        @Override
        public DisburseDestinationType type() {
            return DisburseDestinationType.ACCOUNT;
        }
    }
}
