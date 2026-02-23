package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.math.BigDecimal;
import java.time.Period;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DisburseDestinationRequestDto;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.OriginateLoanFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.PartyRequestDto;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.AmountDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.CurrencyTypeDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.DisburseDestinationDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.EconomicSectorDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.PartyDto;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OriginateLoanFacilityRequestMapper {

    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "loanApplication.branch", ignore = true)
    @Mapping(target = "loanApplication.currency", source = "loanApplication.currency")
    @Mapping(target = "loanApplication.requestedLoanDuration", source = "loanApplication.requestedLoanDurationMonths")
    @Mapping(target = "loanApplication.gracePeriod", source = "loanApplication.gracePeriodDays")
    @Mapping(target = "loanApplication.economicSector", source = "loanApplication.economicSectorCode")
    @Mapping(target = "loanApplication.requestReason.code", source = "loanApplication.requestReasonCode")
    @Mapping(target = "loanApplication.subSource", source = "loanApplication.subSourceCode")
    @Mapping(target = "loanApplication.samat", source = "loanApplication.samat")
    OriginateLoanFacilityCommand toCommand(OriginateLoanFacilityRequest request);

    default DisburseDestinationDto toDisburseDestinationDto(DisburseDestinationRequestDto request) {
        return switch (request) {
            case DisburseDestinationRequestDto.DepositDestinationDto d ->
                new DisburseDestinationDto.DepositDestinationDto(d.depositNumber());
            case DisburseDestinationRequestDto.AccountDestinationDto b ->
                new DisburseDestinationDto.AccountDestinationDto(b.accountNumber());
        };
    }

    default PartyDto toPartyDto(PartyRequestDto dto) {
        return switch (dto) {
            case PartyRequestDto.ApplicantDto d -> new PartyDto.ApplicantDto(d.customerNumber());
            case PartyRequestDto.CoApplicantDto d -> new PartyDto.CoApplicantDto(d.customerNumber());
            case PartyRequestDto.GuarantorDto d ->
                new PartyDto.GuarantorDto(d.customerNumber(), d.guaranteePercentage());
        };
    }

    default AmountDto mapAmount(BigDecimal value) {
        return new AmountDto(value);
    }

    default CurrencyTypeDto mapCurrency(String value) {
        return new CurrencyTypeDto(value);
    }

    default OriginateLoanFacilityCommand.LoanDurationDto mapDuration(Integer months) {
        return new OriginateLoanFacilityCommand.LoanDurationDto(Period.ofMonths(months));
    }

    default OriginateLoanFacilityCommand.GracePeriodDto mapGracePeriod(Integer days) {
        return new OriginateLoanFacilityCommand.GracePeriodDto(days != null ? Period.ofDays(days) : Period.ZERO);
    }

    default OriginateLoanFacilityCommand.InstallmentCountDto mapInstallmentCount(Integer value) {
        return new OriginateLoanFacilityCommand.InstallmentCountDto(value);
    }

    default EconomicSectorDto mapEconomicSector(String code) {
        return new EconomicSectorDto(code);
    }

    default OriginateLoanFacilityCommand.RequestReasonDto mapRequestReason(String code) {
        return new OriginateLoanFacilityCommand.RequestReasonDto(code);
    }

    default OriginateLoanFacilityCommand.SubSourceDto mapSubSource(String code) {
        return code != null ? new OriginateLoanFacilityCommand.SubSourceDto(code) : null;
    }

    default OriginateLoanFacilityCommand.DescriptionDto mapDescription(String value) {
        return value != null ? new OriginateLoanFacilityCommand.DescriptionDto(value) : null;
    }

    default OriginateLoanFacilityCommand.CredibilityRankDto mapCredibilityRank(String value) {
        return value != null ? new OriginateLoanFacilityCommand.CredibilityRankDto(value) : null;
    }
}
