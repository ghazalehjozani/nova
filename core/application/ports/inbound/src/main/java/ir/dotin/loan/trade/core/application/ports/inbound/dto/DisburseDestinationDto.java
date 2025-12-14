package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;

public sealed interface DisburseDestinationDto
        permits DisburseDestinationDto.DepositDestinationDto, DisburseDestinationDto.AccountDestinationDto {

    DisburseDestinationType type();

    record DepositDestinationDto(String depositNumber) implements DisburseDestinationDto {
        @Override
        public DisburseDestinationType type() {
            return DisburseDestinationType.DEPOSIT;
        }
    }

    record AccountDestinationDto(String accountNumber) implements DisburseDestinationDto {
        @Override
        public DisburseDestinationType type() {
            return DisburseDestinationType.ACCOUNT;
        }
    }
}
