package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DefineTradeLoanArrangementRequestToCommandMapper {

    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(source = "code", target = "code.value")
    @Mapping(source = "title", target = "title.value")
    @Mapping(source = "currencyType", target = "currencyType.value")
    @Mapping(source = "economicSector", target = "economicSector.code")
    @Mapping(target = "confirmTypes", qualifiedByName = "mapConfirmTypeList")
    @Mapping(target = "amountRange", qualifiedByName = "mapAmountRange")
    @Mapping(target = "durationRange", qualifiedByName = "mapDurationRange")
    @Mapping(target = "interestPolicy", qualifiedByName = "mapInterestPolicy")
    @Mapping(target = "penaltyPolicy", qualifiedByName = "mapPenaltyPolicy")
    @Mapping(target = "installmentPolicy", qualifiedByName = "mapInstallmentPolicy")
    @Mapping(target = "gracePeriodPolicy", qualifiedByName = "mapGracePeriodPolicy")
    @Mapping(target = "repaymentPriorityPolicy", qualifiedByName = "mapRepaymentPriorityPolicy")
    @Mapping(target = "regulatoryCompliancePolicy", qualifiedByName = "mapRegulatoryCompliancePolicy")
    @Mapping(target = "collateralPolicy", qualifiedByName = "mapCollateralPolicy")
    DefineTradeLoanArrangementCommand toCommand(DefineTradeLoanArrangementRequest request);

    @Named("mapConfirmTypeList")
    default List<DefineTradeLoanArrangementCommand.ConfirmTypeDto> mapConfirmTypeList(List<String> confirmTypes) {
        if (confirmTypes == null) return null;
        return confirmTypes.stream()
                .map(DefineTradeLoanArrangementCommand.ConfirmTypeDto::new)
                .collect(Collectors.toList());
    }

    @Named("mapAmountRange")
    default DefineTradeLoanArrangementCommand.AmountRangeDto mapAmountRange(
            DefineTradeLoanArrangementRequest.AmountRangeDto amountRange) {
        if (amountRange == null) return null;
        return new DefineTradeLoanArrangementCommand.AmountRangeDto(
                new DefineTradeLoanArrangementCommand.MoneyDto(amountRange.min()),
                new DefineTradeLoanArrangementCommand.MoneyDto(amountRange.max()));
    }

    @Named("mapDurationRange")
    default DefineTradeLoanArrangementCommand.LoanDurationRangeDto mapDurationRange(
            DefineTradeLoanArrangementRequest.LoanDurationRangeDto durationRange) {
        if (durationRange == null) return null;
        return new DefineTradeLoanArrangementCommand.LoanDurationRangeDto(
                Period.ofMonths(durationRange.minMonths()), Period.ofMonths(durationRange.maxMonths()));
    }

    @Named("mapInterestPolicy")
    default DefineTradeLoanArrangementCommand.InterestPolicyDto mapInterestPolicy(
            DefineTradeLoanArrangementRequest.InterestPolicyDto interestPolicy) {
        if (interestPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.InterestPolicyDto(
                interestPolicy.minRate(),
                interestPolicy.maxRate(),
                interestPolicy.interestFormula(),
                interestPolicy.refundFormula(),
                interestPolicy.dailyInterest());
    }

    @Named("mapPenaltyPolicy")
    default DefineTradeLoanArrangementCommand.PenaltyPolicyDto mapPenaltyPolicy(
            DefineTradeLoanArrangementRequest.PenaltyPolicyDto penaltyPolicy) {
        if (penaltyPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.PenaltyPolicyDto(
                penaltyPolicy.penaltyRate(),
                penaltyPolicy.deferralInterestRate(),
                penaltyPolicy.formula(),
                penaltyPolicy.paymentType());
    }

    @Named("mapInstallmentPolicy")
    default DefineTradeLoanArrangementCommand.InstallmentPolicyDto mapInstallmentPolicy(
            DefineTradeLoanArrangementRequest.InstallmentPolicyDto installmentPolicy) {
        if (installmentPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.InstallmentPolicyDto(
                Period.ofMonths(installmentPolicy.installmentPeriodMonths()),
                installmentPolicy.installmentFormula(),
                installmentPolicy.interestComponentFormula(),
                installmentPolicy.paymentType());
    }

    @Named("mapGracePeriodPolicy")
    default DefineTradeLoanArrangementCommand.GracePeriodPolicyDto mapGracePeriodPolicy(
            DefineTradeLoanArrangementRequest.GracePeriodPolicyDto gracePeriodPolicy) {
        if (gracePeriodPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.GracePeriodPolicyDto(
                gracePeriodPolicy.minGracePeriodDays(),
                gracePeriodPolicy.maxGracePeriodDays(),
                gracePeriodPolicy.formula());
    }

    @Named("mapRepaymentPriorityPolicy")
    default DefineTradeLoanArrangementCommand.RepaymentPriorityPolicyDto mapRepaymentPriorityPolicy(
            DefineTradeLoanArrangementRequest.RepaymentPriorityPolicyDto repaymentPriorityPolicy) {
        if (repaymentPriorityPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.RepaymentPriorityPolicyDto(
                repaymentPriorityPolicy.principalPriority(),
                repaymentPriorityPolicy.interestPriority(),
                repaymentPriorityPolicy.penaltyPriority(),
                repaymentPriorityPolicy.commissionPriority(),
                repaymentPriorityPolicy.insurancePriority(),
                repaymentPriorityPolicy.insurancePenaltyPriority(),
                repaymentPriorityPolicy.hasEqualPriority());
    }

    @Named("mapRegulatoryCompliancePolicy")
    default DefineTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto mapRegulatoryCompliancePolicy(
            DefineTradeLoanArrangementRequest.RegulatoryCompliancePolicyDto regulatoryCompliancePolicy) {
        if (regulatoryCompliancePolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto(
                regulatoryCompliancePolicy.overDuePeriodMonths(),
                regulatoryCompliancePolicy.deferralPeriodMonths(),
                regulatoryCompliancePolicy.suspiciousPeriodMonths());
    }

    @Named("mapCollateralPolicy")
    default DefineTradeLoanArrangementCommand.CollateralPolicyDto mapCollateralPolicy(
            DefineTradeLoanArrangementRequest.CollateralPolicyDto collateralPolicy) {
        if (collateralPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.CollateralPolicyDto(
                collateralPolicy.totalPercent(),
                collateralPolicy.collateralTypes().stream()
                        .map(DefineTradeLoanArrangementCommand.CollateralTypeDto::new)
                        .collect(Collectors.toSet()),
                collateralPolicy.collateralCalculationType());
    }
}
