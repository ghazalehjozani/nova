package ir.dotin.loan.trade.adapters.driving.messaging.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "role",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PartyRequestDto.ApplicantDto.class, name = "PRIMARY_APPLICANT"),
    @JsonSubTypes.Type(value = PartyRequestDto.CoApplicantDto.class, name = "CO_APPLICANT"),
    @JsonSubTypes.Type(value = PartyRequestDto.GuarantorDto.class, name = "GUARANTOR")
})
public sealed interface PartyRequestDto
        permits PartyRequestDto.ApplicantDto, PartyRequestDto.CoApplicantDto, PartyRequestDto.GuarantorDto {

    String customerNumber();

    PartyRole role();

    record ApplicantDto(String customerNumber, PartyRole role) implements PartyRequestDto {
        public ApplicantDto(String customerNumber) {
            this(customerNumber, PartyRole.PRIMARY_APPLICANT);
        }
    }

    record CoApplicantDto(String customerNumber, PartyRole role) implements PartyRequestDto {
        public CoApplicantDto(String customerNumber) {
            this(customerNumber, PartyRole.CO_APPLICANT);
        }
    }

    record GuarantorDto(String customerNumber, BigDecimal guaranteePercentage, PartyRole role)
            implements PartyRequestDto {
        public GuarantorDto(String customerNumber, BigDecimal guaranteePercentage) {
            this(customerNumber, guaranteePercentage, PartyRole.GUARANTOR);
        }
    }
}
