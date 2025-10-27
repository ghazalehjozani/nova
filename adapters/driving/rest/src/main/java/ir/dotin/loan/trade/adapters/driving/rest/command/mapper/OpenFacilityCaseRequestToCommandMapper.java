package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

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
    @Mapping(target = "loanApplication.customer", source = "loanApplication.customer", qualifiedByName = "mapCustomer")
    @Mapping(
            target = "loanApplication.guarantors",
            source = "loanApplication.guarantors",
            qualifiedByName = "mapGuarantors")
    OpenFacilityCaseCommand toCommand(OpenFacilityCaseRequest request);

    @Named("mapCustomer")
    default OpenFacilityCaseCommand.PartyDto mapCustomer(OpenFacilityCaseRequest.PartyDto customer) {
        return new OpenFacilityCaseCommand.PartyDto(customer.customerNumber(), null, null);
    }

    @Named("mapGuarantors")
    default Set<OpenFacilityCaseCommand.PartyDto> mapGuarantors(Set<OpenFacilityCaseRequest.PartyDto> guarantors) {
        if (guarantors == null) {
            return Set.of();
        }
        return guarantors.stream()
                .map(guarantor -> new OpenFacilityCaseCommand.PartyDto(guarantor.customerNumber(), null, null))
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

    default PlanGradualInstallmentScheduleCommand.MoneyDto mapMoneyToCommandDto(OpenFacilityCaseRequest.MoneyDto dto) {
        return new PlanGradualInstallmentScheduleCommand.MoneyDto(dto.value());
    }
}
