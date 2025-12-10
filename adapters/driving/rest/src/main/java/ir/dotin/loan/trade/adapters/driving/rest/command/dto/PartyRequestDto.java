package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PartyDto", description = "اطلاعات ذینفع")
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

    @Schema(description = "شماره مشتری", requiredMode = Schema.RequiredMode.REQUIRED)
    String customerNumber();

    @Schema(description = "نقش", requiredMode = Schema.RequiredMode.REQUIRED)
    PartyRole role();

    @Schema(name = "ApplicantDto", description = "متقاضی اصلی")
    record ApplicantDto(String customerNumber) implements PartyRequestDto {
        @Override
        public PartyRole role() {
            return PartyRole.PRIMARY_APPLICANT;
        }
    }

    @Schema(name = "CoApplicantDto", description = "متقاضی همراه")
    record CoApplicantDto(String customerNumber) implements PartyRequestDto {
        @Override
        public PartyRole role() {
            return PartyRole.CO_APPLICANT;
        }
    }

    @Schema(name = "GuarantorDto", description = "ضامن")
    record GuarantorDto(
            String customerNumber,

            @Schema(
                    description = "درصد ضمانت",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    minimum = "0",
                    maximum = "100")
            BigDecimal guaranteePercentage)
            implements PartyRequestDto {
        @Override
        public PartyRole role() {
            return PartyRole.GUARANTOR;
        }
    }
}
