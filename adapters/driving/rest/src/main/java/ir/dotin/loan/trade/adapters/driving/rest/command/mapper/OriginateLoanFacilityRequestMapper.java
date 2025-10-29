package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.math.BigDecimal;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.*;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.OriginateLoanFacilityRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OriginateLoanFacilityRequestMapper {

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "loanApplication.applicationNumber", ignore = true)
    @Mapping(target = "loanApplication.branch", ignore = true)
    @Mapping(
            target = "loanApplication.customer",
            source = "loanApplication.customerNumber",
            qualifiedByName = "mapCustomerNumber")
    @Mapping(
            target = "loanApplication.guarantors",
            source = "loanApplication.guarantorNumbers",
            qualifiedByName = "mapGuarantorNumbers")
    @Mapping(
            target = "loanApplication.requestedAmount",
            source = "loanApplication.requestedAmount",
            qualifiedByName = "mapRequestedAmount")
    @Mapping(target = "loanApplication.currency", source = "loanApplication.currency", qualifiedByName = "mapCurrency")
    @Mapping(
            target = "loanApplication.requestedLoanDuration",
            source = "loanApplication.requestedLoanDuration",
            qualifiedByName = "mapRequestedLoanDuration")
    @Mapping(
            target = "loanApplication.gracePeriod",
            source = "loanApplication.gracePeriod",
            qualifiedByName = "mapGracePeriod")
    @Mapping(
            target = "loanApplication.installmentCount",
            source = "loanApplication.installmentCount",
            qualifiedByName = "mapInstallmentCount")
    @Mapping(
            target = "loanApplication.economicSector",
            source = "loanApplication.economicSectorCode",
            qualifiedByName = "mapEconomicSectorCode")
    @Mapping(
            target = "loanApplication.requestReason",
            source = "loanApplication.requestReasonCode",
            qualifiedByName = "mapRequestReasonCode")
    @Mapping(
            target = "loanApplication.subSource",
            source = "loanApplication.subSourceCode",
            qualifiedByName = "mapSubSourceCode")
    @Mapping(
            target = "loanApplication.description",
            source = "loanApplication.description",
            qualifiedByName = "mapDescription")
    @Mapping(
            target = "loanApplication.certificates",
            source = "loanApplication.certificateSerials",
            qualifiedByName = "mapCertificateSerials")
    @Mapping(
            target = "loanApplication.credibilityRank",
            source = "loanApplication.credibilityRank",
            qualifiedByName = "mapCredibilityRank")
    @Mapping(
            target = "loanApplication.disburseDestination.depositNumber",
            source = "loanApplication.disburseDestination.depositNumber")
    @Mapping(target = "loanApplication.disburseDestination.type", source = "loanApplication.disburseDestination.type")
    @Mapping(
            target = "installmentSchedulePlan",
            source = "installmentSchedulePlan",
            qualifiedByName = "mapSchedulePlan")
    OriginateLoanFacilityCommand toCommand(OriginateLoanFacilityRequest request);

    @Named("mapCustomerNumber")
    default OriginateLoanFacilityCommand.PartyDto mapCustomerNumber(String customerNumber) {
        return new OriginateLoanFacilityCommand.PartyDto(customerNumber, null, null);
    }

    @Named("mapGuarantorNumbers")
    default Set<OriginateLoanFacilityCommand.PartyDto> mapGuarantorNumbers(Set<String> guarantorNumbers) {
        if (guarantorNumbers == null) {
            return Set.of();
        }
        return guarantorNumbers.stream()
                .map(number -> new OriginateLoanFacilityCommand.PartyDto(number, null, null))
                .collect(Collectors.toSet());
    }

    @Named("mapSchedulePlan")
    default OriginateLoanFacilityCommand.InstallmentSchedulePlanDto mapSchedulePlan(
            OriginateLoanFacilityRequest.InstallmentSchedulePlanDto dto) {
        if (dto == null) {
            return null;
        }

        List<OriginateLoanFacilityCommand.InstallmentSpecDto> specs =
                dto.installments().stream().map(this::mapInstallmentSpec).toList();

        return OriginateLoanFacilityCommand.InstallmentSchedulePlanDto.builder()
                .installments(specs)
                .build();
    }

    default OriginateLoanFacilityCommand.InstallmentSpecDto mapInstallmentSpec(
            OriginateLoanFacilityRequest.InstallmentSpecDto dto) {
        return OriginateLoanFacilityCommand.InstallmentSpecDto.builder()
                .sequenceNumber(dto.sequenceNumber())
                .dueDate(dto.dueDate())
                .principalAmount(new OriginateLoanFacilityCommand.MoneyDto(dto.principalAmount()))
                .interestAmount(new OriginateLoanFacilityCommand.MoneyDto(dto.interestAmount()))
                .penaltyAmount(
                        dto.penaltyAmount() != null
                                ? new OriginateLoanFacilityCommand.MoneyDto(dto.penaltyAmount())
                                : null)
                .feeAmount(dto.feeAmount() != null ? new OriginateLoanFacilityCommand.MoneyDto(dto.feeAmount()) : null)
                .build();
    }

    @Named("mapRequestedAmount")
    default OriginateLoanFacilityCommand.MoneyDto mapRequestedAmount(BigDecimal amount) {
        return new OriginateLoanFacilityCommand.MoneyDto(amount);
    }

    @Named("mapCurrency")
    default OriginateLoanFacilityCommand.CurrencyTypeDto mapCurrency(String currency) {
        return new OriginateLoanFacilityCommand.CurrencyTypeDto(currency);
    }

    @Named("mapRequestedLoanDuration")
    default OriginateLoanFacilityCommand.LoanDurationDto mapRequestedLoanDuration(Period period) {
        return new OriginateLoanFacilityCommand.LoanDurationDto(period);
    }

    @Named("mapGracePeriod")
    default OriginateLoanFacilityCommand.GracePeriodDto mapGracePeriod(Period period) {
        return new OriginateLoanFacilityCommand.GracePeriodDto(period != null ? period : Period.ZERO);
    }

    @Named("mapInstallmentCount")
    default OriginateLoanFacilityCommand.InstallmentCountDto mapInstallmentCount(Integer count) {
        return new OriginateLoanFacilityCommand.InstallmentCountDto(count);
    }

    @Named("mapEconomicSectorCode")
    default OriginateLoanFacilityCommand.EconomicSectorDto mapEconomicSectorCode(String code) {
        return new OriginateLoanFacilityCommand.EconomicSectorDto(code);
    }

    @Named("mapRequestReasonCode")
    default OriginateLoanFacilityCommand.RequestReasonDto mapRequestReasonCode(String code) {
        return new OriginateLoanFacilityCommand.RequestReasonDto(code);
    }

    @Named("mapSubSourceCode")
    default OriginateLoanFacilityCommand.SubSourceDto mapSubSourceCode(String code) {
        return code != null ? new OriginateLoanFacilityCommand.SubSourceDto(code) : null;
    }

    @Named("mapDescription")
    default OriginateLoanFacilityCommand.DescriptionDto mapDescription(String description) {
        return description != null ? new OriginateLoanFacilityCommand.DescriptionDto(description) : null;
    }

    @Named("mapCertificateSerials")
    default Set<OriginateLoanFacilityCommand.CertificateDto> mapCertificateSerials(Set<String> certificateSerials) {
        if (certificateSerials == null) {
            return Set.of();
        }
        return certificateSerials.stream()
                .map(OriginateLoanFacilityCommand.CertificateDto::new)
                .collect(Collectors.toSet());
    }

    @Named("mapCredibilityRank")
    default OriginateLoanFacilityCommand.CredibilityRankDto mapCredibilityRank(String rank) {
        return rank != null ? new OriginateLoanFacilityCommand.CredibilityRankDto(rank) : null;
    }
}
