package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.UUID;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.FullLoanFacilityLifecycleRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.AmountDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.CurrencyTypeDto;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {UUID.class, BigDecimal.class, ZoneOffset.class, AmountDto.class, Period.class})
public interface FullLoanFacilityLifecycleRequestMapper {

    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "transactionMetadata", ignore = true)
    FullLoanFacilityLifecycleCommand toCommand(FullLoanFacilityLifecycleRequest request);

    @Mapping(target = "economicSector.code", source = "economicSectorCode")
    @Mapping(target = "branch.code", source = "branchCode")
    @Mapping(target = "subSource.code", source = "subSourceCode")
    @Mapping(target = "description.value", source = "description")
    @Mapping(
            target = "requestedLoanDuration.value",
            source = "requestedLoanDurationMonths",
            qualifiedByName = "mapMonthsToPeriod")
    @Mapping(target = "gracePeriod.value", source = "gracePeriodDays", qualifiedByName = "mapDaysToPeriod")
    @Mapping(target = "installmentCount.value", source = "installmentCount")
    @Mapping(target = "applicationNumber", source = "applicationNumber", qualifiedByName = "mapApplicationNumber")
    FullLoanFacilityLifecycleCommand.LoanApplicationDto toLoanApplicationDto(
            FullLoanFacilityLifecycleRequest.LoanApplicationDto dto);

    FullLoanFacilityLifecycleCommand.PartyDto toPartyDto(FullLoanFacilityLifecycleRequest.PartyDto dto);

    FullLoanFacilityLifecycleCommand.RequestReasonDto toRequestReasonDto(
            FullLoanFacilityLifecycleRequest.RequestReasonDto dto);

    FullLoanFacilityLifecycleCommand.DisburseDestinationDto toDisburseDestinationDto(
            FullLoanFacilityLifecycleRequest.DisburseDestinationDto dto);

    FullLoanFacilityLifecycleCommand.InstallmentSchedulePlanDto toInstallmentSchedulePlanDto(
            FullLoanFacilityLifecycleRequest.InstallmentSchedulePlanDto dto);

    @Mapping(target = "dueDate", source = "dueDate", qualifiedByName = "mapInstantToLocalDate")
    @Mapping(target = "interestAmount", ignore = true)
    @Mapping(target = "penaltyAmount", ignore = true)
    @Mapping(target = "feeAmount", ignore = true)
    FullLoanFacilityLifecycleCommand.InstallmentSpecDto toInstallmentSpecDto(
            FullLoanFacilityLifecycleRequest.InstallmentSpecDto dto);

    @AfterMapping
    default void setInterestAmount(
            @MappingTarget FullLoanFacilityLifecycleCommand.InstallmentSpecDto.InstallmentSpecDtoBuilder builder) {
        builder.interestAmount(new AmountDto(BigDecimal.ZERO));
    }

    @Named("mapInstantToLocalDate")
    default LocalDate mapInstantToLocalDate(Instant instant) {
        if (instant == null) return null;
        return LocalDate.ofInstant(instant, ZoneOffset.UTC);
    }

    @Named("mapMonthsToPeriod")
    default Period mapMonthsToPeriod(Integer months) {
        if (months == null) return null;
        return Period.ofMonths(months);
    }

    @Named("mapDaysToPeriod")
    default Period mapDaysToPeriod(Integer days) {
        if (days == null) return null;
        return Period.ofDays(days);
    }

    @Named("mapApplicationNumber")
    default FullLoanFacilityLifecycleCommand.ApplicationNumberDto mapApplicationNumber(String appNum) {
        if (appNum == null) return null;
        return FullLoanFacilityLifecycleCommand.ApplicationNumberDto.builder()
                .derivedValue(appNum)
                .build();
    }

    default AmountDto mapAmount(BigDecimal value) {
        return value != null ? new AmountDto(value) : null;
    }

    default CurrencyTypeDto mapCurrency(String value) {
        return value != null ? new CurrencyTypeDto(value) : null;
    }

    default FullLoanFacilityLifecycleCommand.CredibilityRankDto mapCredibilityRank(String value) {
        return value != null ? new FullLoanFacilityLifecycleCommand.CredibilityRankDto(value) : null;
    }
}
