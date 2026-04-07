package ir.dotin.loan.trade.adapters.driving.contract.dto;

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

    record DepositDestinationDto(String depositNumber, DisburseDestinationType type)
            implements DisburseDestinationRequestDto {
        public DepositDestinationDto(String depositNumber) {
            this(depositNumber, DisburseDestinationType.DEPOSIT);
        }
    }

    record AccountDestinationDto(String accountNumber, DisburseDestinationType type)
            implements DisburseDestinationRequestDto {
        public AccountDestinationDto(String accountNumber) {
            this(accountNumber, DisburseDestinationType.ACCOUNT);
        }
    }
}
