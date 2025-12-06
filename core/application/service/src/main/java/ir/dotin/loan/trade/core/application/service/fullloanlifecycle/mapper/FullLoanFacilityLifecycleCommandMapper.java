package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.mapper;

import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.EconomicSectorDto;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;

@Mapper(config = BaseMapperConfig.class)
public interface FullLoanFacilityLifecycleCommandMapper {

    default OriginateLoanFacilityCommand toOriginationCommand(FullLoanFacilityLifecycleCommand command) {
        return OriginateLoanFacilityCommand.builder()
                .uid(command.uid())
                .version(command.version())
                .loanTypeCode(command.loanTypeCode())
                .loanArrangementCode(command.loanArrangementCode())
                .loanApplication(toOriginationLoanApplicationDto(command.loanApplication()))
                .installmentSchedulePlan(
                        command.installmentSchedulePlan() != null
                                ? toOriginationSchedulePlan(command.installmentSchedulePlan())
                                : null)
                .build();
    }

    default OriginateLoanFacilityCommand.LoanApplicationDto toOriginationLoanApplicationDto(
            FullLoanFacilityLifecycleCommand.LoanApplicationDto app) {
        return OriginateLoanFacilityCommand.LoanApplicationDto.builder()
                .requestDate(app.requestDate())
                .parties(app.parties().stream().map(this::toOriginationPartyDto).collect(Collectors.toSet()))
                .requestedAmount(app.requestedAmount())
                .currency(app.currency())
                .requestedLoanDuration(toOriginationLoanDuration(app.requestedLoanDuration()))
                .applicantChannel(app.applicantChannel())
                .gracePeriod(toOriginationGracePeriod(app.gracePeriod()))
                .installmentCount(toOriginationInstallmentCount(app.installmentCount()))
                .disburseDestination(toOriginationDisburseDestination(app.disburseDestination()))
                .economicSector(toOriginationEconomicSector(app.economicSector()))
                .branch(toOriginationBranch(app.branch()))
                .requestReason(toOriginationRequestReason(app.requestReason()))
                .subSource(toOriginationSubSource(app.subSource()))
                .description(toOriginationDescription(app.description()))
                .certificates(app.certificates().stream()
                        .map(this::toOriginationCertificate)
                        .collect(Collectors.toSet()))
                .applicationNumber(toOriginationApplicationNumber(app.applicationNumber()))
                .disbursementMethod(app.disbursementMethod())
                .credibilityRank(toOriginationCredibilityRank(app.credibilityRank()))
                .build();
    }

    default OriginateLoanFacilityCommand.PartyDto toOriginationPartyDto(
            FullLoanFacilityLifecycleCommand.PartyDto party) {
        return OriginateLoanFacilityCommand.PartyDto.builder()
                .customerNumber(party.customerNumber())
                .role(party.role())
                .build();
    }

    default OriginateLoanFacilityCommand.DisburseDestinationDto toOriginationDisburseDestination(
            FullLoanFacilityLifecycleCommand.DisburseDestinationDto dest) {
        return OriginateLoanFacilityCommand.DisburseDestinationDto.builder()
                .type(dest.type())
                .depositNumber(dest.depositNumber())
                .build();
    }

    default OriginateLoanFacilityCommand.RequestReasonDto toOriginationRequestReason(
            FullLoanFacilityLifecycleCommand.RequestReasonDto reason) {
        if (reason == null) return null;
        return OriginateLoanFacilityCommand.RequestReasonDto.builder()
                .code(reason.code())
                .build();
    }

    default OriginateLoanFacilityCommand.CertificateDto toOriginationCertificate(
            FullLoanFacilityLifecycleCommand.CertificateDto cert) {
        if (cert == null) return null;
        return OriginateLoanFacilityCommand.CertificateDto.builder()
                .serial(cert.serial())
                .build();
    }

    default OriginateLoanFacilityCommand.InstallmentSchedulePlanDto toOriginationSchedulePlan(
            FullLoanFacilityLifecycleCommand.InstallmentSchedulePlanDto plan) {
        if (plan == null) return null;
        return OriginateLoanFacilityCommand.InstallmentSchedulePlanDto.builder()
                .installments(plan.installments().stream()
                        .map(this::toOriginationInstallmentSpec)
                        .toList())
                .build();
    }

    default OriginateLoanFacilityCommand.InstallmentSpecDto toOriginationInstallmentSpec(
            FullLoanFacilityLifecycleCommand.InstallmentSpecDto spec) {
        return OriginateLoanFacilityCommand.InstallmentSpecDto.builder()
                .sequenceNumber(spec.sequenceNumber())
                .dueDate(spec.dueDate())
                .principalAmount(spec.principalAmount())
                .interestAmount(spec.interestAmount())
                .penaltyAmount(spec.penaltyAmount())
                .feeAmount(spec.feeAmount())
                .build();
    }

    default OriginateLoanFacilityCommand.LoanDurationDto toOriginationLoanDuration(
            FullLoanFacilityLifecycleCommand.LoanDurationDto dto) {
        if (dto == null) return null;
        return OriginateLoanFacilityCommand.LoanDurationDto.builder()
                .value(dto.value())
                .build();
    }

    default OriginateLoanFacilityCommand.GracePeriodDto toOriginationGracePeriod(
            FullLoanFacilityLifecycleCommand.GracePeriodDto dto) {
        if (dto == null) return null;
        return OriginateLoanFacilityCommand.GracePeriodDto.builder()
                .value(dto.value())
                .build();
    }

    default OriginateLoanFacilityCommand.InstallmentCountDto toOriginationInstallmentCount(
            FullLoanFacilityLifecycleCommand.InstallmentCountDto dto) {
        if (dto == null) return null;
        return OriginateLoanFacilityCommand.InstallmentCountDto.builder()
                .value(dto.value())
                .build();
    }

    default EconomicSectorDto toOriginationEconomicSector(FullLoanFacilityLifecycleCommand.EconomicSectorDto dto) {
        if (dto == null) return null;
        return EconomicSectorDto.builder().code(dto.code()).build();
    }

    default OriginateLoanFacilityCommand.BranchDto toOriginationBranch(FullLoanFacilityLifecycleCommand.BranchDto dto) {
        if (dto == null) return null;
        return OriginateLoanFacilityCommand.BranchDto.builder().code(dto.code()).build();
    }

    default OriginateLoanFacilityCommand.SubSourceDto toOriginationSubSource(
            FullLoanFacilityLifecycleCommand.SubSourceDto dto) {
        if (dto == null) return null;
        return OriginateLoanFacilityCommand.SubSourceDto.builder()
                .code(dto.code())
                .build();
    }

    default OriginateLoanFacilityCommand.DescriptionDto toOriginationDescription(
            FullLoanFacilityLifecycleCommand.DescriptionDto dto) {
        if (dto == null) return null;
        return OriginateLoanFacilityCommand.DescriptionDto.builder()
                .value(dto.value())
                .build();
    }

    default OriginateLoanFacilityCommand.ApplicationNumberDto toOriginationApplicationNumber(
            FullLoanFacilityLifecycleCommand.ApplicationNumberDto dto) {
        if (dto == null) return null;
        return OriginateLoanFacilityCommand.ApplicationNumberDto.builder()
                .branch(toOriginationBranch(dto.branch()))
                .derivedValue(dto.derivedValue())
                .build();
    }

    default OriginateLoanFacilityCommand.CredibilityRankDto toOriginationCredibilityRank(
            FullLoanFacilityLifecycleCommand.CredibilityRankDto dto) {
        if (dto == null) return null;
        return OriginateLoanFacilityCommand.CredibilityRankDto.builder()
                .value(dto.value())
                .build();
    }

    default TransactionConfig toTransactionConfig(FullLoanFacilityLifecycleCommand.TransactionMetadataDto ctx) {
        if (ctx == null) return null;
        return TransactionConfig.builder()
                .branchCode(ctx.branchCode())
                .userId(ctx.userId())
                .terminalId(ctx.terminalId())
                .terminalIp(ctx.terminalIp())
                .terminalType(ctx.terminalType())
                .channel(ctx.channel())
                .toolSource(ctx.toolSource())
                .productCode(ctx.productCode())
                .networkType(ctx.networkType())
                .build();
    }
}
