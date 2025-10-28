package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.math.BigDecimal;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mapstruct.*;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.OpenFacilityCaseRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OpenFacilityCaseCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanGradualInstallmentScheduleCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OpenFacilityCaseRequestToCommandMapper {

    @Mapping(target = "installmentSchedule", source = "installmentSchedule")
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
            source = "loanApplication.disburseDestination.depositNumber",
            qualifiedByName = "mapDisbursementDepositNumber")
    @Mapping(target = "loanApplication.disburseDestination.type", source = "loanApplication.disburseDestination.type")
    OpenFacilityCaseCommand toCommand(OpenFacilityCaseRequest request);

    @Named("mapCustomerNumber")
    default OpenFacilityCaseCommand.PartyDto mapCustomerNumber(String customerNumber) {
        return new OpenFacilityCaseCommand.PartyDto(customerNumber, null, null);
    }

    @Named("mapGuarantorNumbers")
    default Set<OpenFacilityCaseCommand.PartyDto> mapGuarantorNumbers(Set<String> guarantorNumbers) {
        if (guarantorNumbers == null) {
            return Set.of();
        }
        return guarantorNumbers.stream()
                .map(guarantorNumber -> new OpenFacilityCaseCommand.PartyDto(guarantorNumber, null, null))
                .collect(Collectors.toSet());
    }

    default Optional<PlanGradualInstallmentScheduleCommand> mapSchedule(
            OpenFacilityCaseRequest.PlanGradualInstallmentScheduleDTO dto) {
        if (dto == null) {
            return Optional.empty();
        }

        return Optional.of(PlanGradualInstallmentScheduleCommand.builder()
                .uid(UUID.randomUUID())
                .totalLoanAmount(mapMoneyToCommandDto(dto.totalLoanAmount()))
                .interestRate(dto.interestRate())
                .gracePeriodDays(dto.gracePeriodDays())
                .installments(
                        dto.installments() != null
                                ? dto.installments().stream()
                                        .map(this::mapInstallmentSpec)
                                        .toList()
                                : List.of())
                .build());
    }

    default PlanGradualInstallmentScheduleCommand.InstallmentSpecDto mapInstallmentSpec(
            OpenFacilityCaseRequest.InstallmentSpecDto dto) {
        return new PlanGradualInstallmentScheduleCommand.InstallmentSpecDto(
                dto.dueDate(), mapMoneyToCommandDto(dto.principalAmount()), mapMoneyToCommandDto(dto.interestAmount()));
    }

    default PlanGradualInstallmentScheduleCommand.MoneyDto mapMoneyToCommandDto(BigDecimal amount) {
        return new PlanGradualInstallmentScheduleCommand.MoneyDto(amount);
    }

    @Named("mapRequestedAmount")
    default OpenFacilityCaseCommand.MoneyDto mapRequestedAmount(BigDecimal amount) {
        return new OpenFacilityCaseCommand.MoneyDto(amount);
    }

    @Named("mapCurrency")
    default OpenFacilityCaseCommand.CurrencyTypeDto mapCurrency(String currency) {
        return new OpenFacilityCaseCommand.CurrencyTypeDto(currency);
    }

    @Named("mapRequestedLoanDuration")
    default OpenFacilityCaseCommand.LoanDurationDto mapRequestedLoanDuration(Period period) {
        return new OpenFacilityCaseCommand.LoanDurationDto(period);
    }

    @Named("mapGracePeriod")
    default OpenFacilityCaseCommand.GracePeriodDto mapGracePeriod(Period period) {
        return new OpenFacilityCaseCommand.GracePeriodDto(period);
    }

    @Named("mapInstallmentCount")
    default OpenFacilityCaseCommand.InstallmentCountDto mapInstallmentCount(Integer count) {
        return new OpenFacilityCaseCommand.InstallmentCountDto(count);
    }

    @Named("mapEconomicSectorCode")
    default OpenFacilityCaseCommand.EconomicSectorDto mapEconomicSectorCode(String code) {
        return new OpenFacilityCaseCommand.EconomicSectorDto(code);
    }

    @Named("mapRequestReasonCode")
    default OpenFacilityCaseCommand.RequestReasonDto mapRequestReasonCode(String code) {
        return new OpenFacilityCaseCommand.RequestReasonDto(code);
    }

    @Named("mapSubSourceCode")
    default OpenFacilityCaseCommand.SubSourceDto mapSubSourceCode(String code) {
        return new OpenFacilityCaseCommand.SubSourceDto(code);
    }

    @Named("mapDescription")
    default OpenFacilityCaseCommand.DescriptionDto mapDescription(String description) {
        return new OpenFacilityCaseCommand.DescriptionDto(description);
    }

    @Named("mapCertificateSerials")
    default Set<OpenFacilityCaseCommand.CertificateDto> mapCertificateSerials(Set<String> certificateSerials) {
        if (certificateSerials == null) {
            return Set.of();
        }
        return certificateSerials.stream()
                .map(OpenFacilityCaseCommand.CertificateDto::new)
                .collect(Collectors.toSet());
    }

    @Named("mapCredibilityRank")
    default OpenFacilityCaseCommand.CredibilityRankDto mapCredibilityRank(String rank) {
        return new OpenFacilityCaseCommand.CredibilityRankDto(rank);
    }

    @Named("mapDisbursementDepositNumber")
    default String mapDisbursementDepositNumber(String depositNumber) {
        return depositNumber;
    }
}
