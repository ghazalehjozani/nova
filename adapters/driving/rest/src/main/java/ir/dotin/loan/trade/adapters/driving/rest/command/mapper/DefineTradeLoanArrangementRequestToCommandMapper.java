package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.util.stream.Collectors;

import org.mapstruct.*;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DefineTradeLoanArrangementRequestToCommandMapper {

    @Mapping(target = "version", ignore = true)
    @Mapping(source = "code", target = "code.value")
    @Mapping(source = "title", target = "title.value")
    @Mapping(source = "currencyType", target = "currencyType.value")
    @Mapping(source = "economicSector", target = "economicSector.code")
    @Mapping(source = "confirmType", target = "confirmType.personCode")
    @Mapping(target = "amountRange", qualifiedByName = "mapAmountRange")
    @Mapping(target = "interestPolicy", qualifiedByName = "mapInterestPolicy")
    @Mapping(target = "penaltyPolicy", qualifiedByName = "mapPenaltyPolicy")
    @Mapping(target = "installmentPolicy", qualifiedByName = "mapInstallmentPolicy")
    @Mapping(target = "gracePeriodPolicy", qualifiedByName = "mapGracePeriodPolicy")
    @Mapping(target = "repaymentPriorityPolicy", qualifiedByName = "mapRepaymentPriorityPolicy")
    @Mapping(target = "regulatoryCompliancePolicy", qualifiedByName = "mapRegulatoryCompliancePolicy")
    @Mapping(target = "collateralPolicy", qualifiedByName = "mapCollateralPolicy")
    DefineTradeLoanArrangementCommand toCommand(DefineTradeLoanArrangementRequest request);

    @Named("mapAmountRange")
    default DefineTradeLoanArrangementCommand.AmountRangeDto mapAmountRange(
            DefineTradeLoanArrangementRequest.AmountRangeDto amountRange) {
        return new DefineTradeLoanArrangementCommand.AmountRangeDto(
                new DefineTradeLoanArrangementCommand.MoneyDto(amountRange.min()),
                new DefineTradeLoanArrangementCommand.MoneyDto(amountRange.max()));
    }

    @Named("mapInterestPolicy")
    default DefineTradeLoanArrangementCommand.InterestPolicyDto mapInterestPolicy(
            DefineTradeLoanArrangementRequest.InterestPolicyDto interestPolicy) {
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
        return new DefineTradeLoanArrangementCommand.PenaltyPolicyDto(
                penaltyPolicy.penaltyRate(),
                penaltyPolicy.deferralInterestRate(),
                penaltyPolicy.formula(),
                penaltyPolicy.paymentType());
    }

    @Named("mapInstallmentPolicy")
    default DefineTradeLoanArrangementCommand.InstallmentPolicyDto mapInstallmentPolicy(
            DefineTradeLoanArrangementRequest.InstallmentPolicyDto installmentPolicy) {
        return new DefineTradeLoanArrangementCommand.InstallmentPolicyDto(
                installmentPolicy.installmentPeriod(),
                installmentPolicy.installmentFormula(),
                installmentPolicy.interestComponentFormula(),
                installmentPolicy.paymentType(),
                installmentPolicy.isDefineAutomaticInstallment());
    }

    @Named("mapGracePeriodPolicy")
    default DefineTradeLoanArrangementCommand.GracePeriodPolicyDto mapGracePeriodPolicy(
            DefineTradeLoanArrangementRequest.GracePeriodPolicyDto gracePeriodPolicy) {
        return new DefineTradeLoanArrangementCommand.GracePeriodPolicyDto(
                gracePeriodPolicy.minGracePeriodDays(),
                gracePeriodPolicy.maxGracePeriodDays(),
                gracePeriodPolicy.formula());
    }

    @Named("mapRepaymentPriorityPolicy")
    default DefineTradeLoanArrangementCommand.RepaymentPriorityPolicyDto mapRepaymentPriorityPolicy(
            DefineTradeLoanArrangementRequest.RepaymentPriorityPolicyDto repaymentPriorityPolicy) {
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
        return new DefineTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto(
                regulatoryCompliancePolicy.overDuePeriod(),
                regulatoryCompliancePolicy.deferralPeriod(),
                regulatoryCompliancePolicy.suspiciousPeriod());
    }

    @Named("mapCollateralPolicy")
    default DefineTradeLoanArrangementCommand.CollateralPolicyDto mapCollateralPolicy(
            DefineTradeLoanArrangementRequest.CollateralPolicyDto collateralPolicy) {
        return new DefineTradeLoanArrangementCommand.CollateralPolicyDto(
                collateralPolicy.totalPercent(),
                collateralPolicy.collateralTypes().stream()
                        .map(DefineTradeLoanArrangementCommand.CollateralTypeDto::new)
                        .collect(Collectors.toSet()),
                collateralPolicy.collateralCalculationType());
    }
}
