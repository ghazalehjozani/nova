package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.contract.dto.DisburseDestinationRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.contract.dto.PartyRequestDto;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.AmountDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.CurrencyTypeDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.DisburseDestinationDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.PartyDto;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {UUID.class, BigDecimal.class, ZoneOffset.class, AmountDto.class, Period.class})
public interface FullLoanFacilityLifecycleMessageMapper {

    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "transactionMetadata", ignore = true)
    @Mapping(target = "collaterals", source = "collaterals", qualifiedByName = "mapCollateralsList")
    @Mapping(target = "confirmType", source = "confirmType")
    FullLoanFacilityLifecycleCommand toCommand(FullLoanFacilityLifecycleMessage request);

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
    @Mapping(target = "samatDto", source = "samat")
    FullLoanFacilityLifecycleCommand.LoanApplicationDto toLoanApplicationDto(
            FullLoanFacilityLifecycleMessage.LoanApplicationDto dto);

    static DisburseDestinationDto toDisburseDestinationDto(DisburseDestinationRequestDto request) {
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

    FullLoanFacilityLifecycleCommand.RequestReasonDto toRequestReasonDto(
            FullLoanFacilityLifecycleMessage.RequestReasonDto dto);

    FullLoanFacilityLifecycleCommand.InstallmentSchedulePlanDto toInstallmentSchedulePlanDto(
            FullLoanFacilityLifecycleMessage.InstallmentSchedulePlanDto dto);

    @Mapping(target = "dueDate", source = "dueDate", qualifiedByName = "mapInstantToLocalDate")
    FullLoanFacilityLifecycleCommand.InstallmentSpecDto toInstallmentSpecDto(
            FullLoanFacilityLifecycleMessage.InstallmentSpecDto dto);

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

    default AmountDto mapAmount(BigDecimal value) {
        return value != null ? new AmountDto(value) : null;
    }

    default CurrencyTypeDto mapCurrency(String value) {
        return value != null ? new CurrencyTypeDto(value) : null;
    }

    default FullLoanFacilityLifecycleCommand.CredibilityRankDto mapCredibilityRank(String value) {
        return value != null ? new FullLoanFacilityLifecycleCommand.CredibilityRankDto(value) : null;
    }

    @Named("mapCollateralsList")
    default List<FullLoanFacilityLifecycleCommand.CollateralDto> mapCollateralsList(
            List<FullLoanFacilityLifecycleMessage.CollateralDto> collaterals) {
        if (collaterals == null || collaterals.isEmpty()) {
            return List.of();
        }
        return collaterals.stream().map(this::mapCollateral).toList();
    }

    @Mapping(target = "usedAmount", source = "usedAmount")
    FullLoanFacilityLifecycleCommand.CollateralDto mapCollateral(FullLoanFacilityLifecycleMessage.CollateralDto dto);

    FullLoanFacilityLifecycleCommand.MoneyDto mapMoneyDto(FullLoanFacilityLifecycleMessage.MoneyDto dto);
}
