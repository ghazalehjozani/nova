package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.contract.dto.DefineTradeLoanArrangementRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.*;

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
    DefineTradeLoanArrangementCommand toCommand(DefineTradeLoanArrangementRequest request);

    default List<DefineTradeLoanArrangementCommand.ConfirmTypeDto> mapConfirmTypeList(List<String> confirmTypes) {
        if (confirmTypes == null) return Collections.emptyList();
        return confirmTypes.stream()
                .map(DefineTradeLoanArrangementCommand.ConfirmTypeDto::new)
                .toList();
    }

    default DefineTradeLoanArrangementCommand.@Nullable AmountRangeDto mapAmountRange(
            DefineTradeLoanArrangementRequest.@Nullable AmountRangeDto amountRange) {
        if (amountRange == null) return null;
        return new DefineTradeLoanArrangementCommand.AmountRangeDto(
                new AmountDto(amountRange.min()), new AmountDto(amountRange.max()));
    }

    default DefineTradeLoanArrangementCommand.@Nullable LoanDurationRangeDto mapDurationRange(
            DefineTradeLoanArrangementRequest.@Nullable LoanDurationRangeDto durationRange) {
        if (durationRange == null) return null;
        return new DefineTradeLoanArrangementCommand.LoanDurationRangeDto(
                Period.ofMonths(durationRange.minMonths()), Period.ofMonths(durationRange.maxMonths()));
    }

    default DefineTradeLoanArrangementCommand.@Nullable InterestPolicyDto mapInterestPolicy(
            DefineTradeLoanArrangementRequest.@Nullable InterestPolicyDto interestPolicy) {
        if (interestPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.InterestPolicyDto(
                interestPolicy.rate(),
                interestPolicy.minPreferentialRate(),
                interestPolicy.maxPreferentialRate(),
                interestPolicy.interestFormula(),
                interestPolicy.refundFormula(),
                interestPolicy.dailyInterest());
    }

    default DefineTradeLoanArrangementCommand.@Nullable PenaltyPolicyDto mapPenaltyPolicy(
            DefineTradeLoanArrangementRequest.@Nullable PenaltyPolicyDto penaltyPolicy) {
        if (penaltyPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.PenaltyPolicyDto(
                penaltyPolicy.penaltyRate(),
                penaltyPolicy.deferralInterestRate(),
                penaltyPolicy.formula(),
                penaltyPolicy.paymentType());
    }

    default DefineTradeLoanArrangementCommand.@Nullable InstallmentPolicyDto mapInstallmentPolicy(
            DefineTradeLoanArrangementRequest.@Nullable InstallmentPolicyDto installmentPolicy) {
        if (installmentPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.InstallmentPolicyDto(
                Period.ofMonths(installmentPolicy.installmentPeriodMonths()),
                installmentPolicy.installmentFormula(),
                installmentPolicy.interestComponentFormula(),
                installmentPolicy.paymentType());
    }

    default DefineTradeLoanArrangementCommand.@Nullable GracePeriodPolicyDto mapGracePeriodPolicy(
            DefineTradeLoanArrangementRequest.@Nullable GracePeriodPolicyDto gracePeriodPolicy) {
        if (gracePeriodPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.GracePeriodPolicyDto(
                gracePeriodPolicy.minGracePeriodDays(),
                gracePeriodPolicy.maxGracePeriodDays(),
                gracePeriodPolicy.formula());
    }

    default DefineTradeLoanArrangementCommand.@Nullable RepaymentPriorityPolicyDto mapRepaymentPriorityPolicy(
            DefineTradeLoanArrangementRequest.@Nullable RepaymentPriorityPolicyDto repaymentPriorityPolicy) {
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

    default DefineTradeLoanArrangementCommand.@Nullable RegulatoryCompliancePolicyDto mapRegulatoryCompliancePolicy(
            DefineTradeLoanArrangementRequest.@Nullable RegulatoryCompliancePolicyDto regulatoryCompliancePolicy) {
        if (regulatoryCompliancePolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto(
                regulatoryCompliancePolicy.overDuePeriodMonths(),
                regulatoryCompliancePolicy.deferralPeriodMonths(),
                regulatoryCompliancePolicy.suspiciousPeriodMonths());
    }

    default DefineTradeLoanArrangementCommand.@Nullable CollateralPolicyDto mapCollateralPolicy(
            DefineTradeLoanArrangementRequest.@Nullable CollateralPolicyDto collateralPolicy) {
        if (collateralPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.CollateralPolicyDto(
                collateralPolicy.totalPercent(),
                collateralPolicy.collateralTypes().stream()
                        .map(DefineTradeLoanArrangementCommand.CollateralTypeDto::new)
                        .collect(Collectors.toSet()),
                collateralPolicy.collateralCalculationType());
    }
}
