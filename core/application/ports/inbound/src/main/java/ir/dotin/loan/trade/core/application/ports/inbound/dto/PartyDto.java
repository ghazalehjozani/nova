package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import java.math.BigDecimal;

import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;

public sealed interface PartyDto permits PartyDto.ApplicantDto, PartyDto.CoApplicantDto, PartyDto.GuarantorDto {

    String customerNumber();

    PartyRole role();

    record ApplicantDto(String customerNumber) implements PartyDto {
        @Override
        public PartyRole role() {
            return PartyRole.PRIMARY_APPLICANT;
        }
    }

    record CoApplicantDto(String customerNumber) implements PartyDto {
        @Override
        public PartyRole role() {
            return PartyRole.CO_APPLICANT;
        }
    }

    record GuarantorDto(String customerNumber, BigDecimal guaranteePercentage) implements PartyDto {
        @Override
        public PartyRole role() {
            return PartyRole.GUARANTOR;
        }
    }
}
