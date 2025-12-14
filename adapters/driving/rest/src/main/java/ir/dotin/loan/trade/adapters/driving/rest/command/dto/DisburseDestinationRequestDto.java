package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DisburseDestinationDto", description = "مقصد تخصیص")
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

    @Schema(description = "نوع مقصد", requiredMode = Schema.RequiredMode.REQUIRED)
    DisburseDestinationType type();

    @Schema(name = "DepositDestinationDto", description = "مقصد سپرده")
    record DepositDestinationDto(
            @Schema(description = "شماره سپرده", requiredMode = Schema.RequiredMode.REQUIRED)
            String depositNumber) implements DisburseDestinationRequestDto {
        @Override
        public DisburseDestinationType type() {
            return DisburseDestinationType.DEPOSIT;
        }
    }

    @Schema(name = "AccountDestinationDto", description = "مقصد حساب")
    record AccountDestinationDto(
            @Schema(description = "شماره حساب", requiredMode = Schema.RequiredMode.REQUIRED)
            String accountNumber) implements DisburseDestinationRequestDto {
        @Override
        public DisburseDestinationType type() {
            return DisburseDestinationType.ACCOUNT;
        }
    }
}
