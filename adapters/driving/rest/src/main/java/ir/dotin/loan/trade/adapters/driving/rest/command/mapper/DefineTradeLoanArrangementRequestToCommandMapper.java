package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest;
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

    default DefineTradeLoanArrangementCommand.AmountRangeDto mapAmountRange(
            DefineTradeLoanArrangementRequest.AmountRangeDto amountRange) {
        if (amountRange == null) return null;
        return new DefineTradeLoanArrangementCommand.AmountRangeDto(
                new AmountDto(amountRange.min()), new AmountDto(amountRange.max()));
    }

    default DefineTradeLoanArrangementCommand.LoanDurationRangeDto mapDurationRange(
            DefineTradeLoanArrangementRequest.LoanDurationRangeDto durationRange) {
        if (durationRange == null) return null;
        return new DefineTradeLoanArrangementCommand.LoanDurationRangeDto(
                Period.ofMonths(durationRange.minMonths()), Period.ofMonths(durationRange.maxMonths()));
    }

    default DefineTradeLoanArrangementCommand.InterestPolicyDto mapInterestPolicy(
            DefineTradeLoanArrangementRequest.InterestPolicyDto interestPolicy) {
        if (interestPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.InterestPolicyDto(
                interestPolicy.rate(),
                interestPolicy.minPreferentialRate(),
                interestPolicy.maxPreferentialRate(),
                interestPolicy.interestFormula(),
                interestPolicy.refundFormula(),
                interestPolicy.dailyInterest());
    }

    default DefineTradeLoanArrangementCommand.PenaltyPolicyDto mapPenaltyPolicy(
            DefineTradeLoanArrangementRequest.PenaltyPolicyDto penaltyPolicy) {
        if (penaltyPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.PenaltyPolicyDto(
                penaltyPolicy.penaltyRate(),
                penaltyPolicy.deferralInterestRate(),
                penaltyPolicy.formula(),
                penaltyPolicy.paymentType());
    }

    default DefineTradeLoanArrangementCommand.InstallmentPolicyDto mapInstallmentPolicy(
            DefineTradeLoanArrangementRequest.InstallmentPolicyDto installmentPolicy) {
        if (installmentPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.InstallmentPolicyDto(
                Period.ofMonths(installmentPolicy.installmentPeriodMonths()),
                installmentPolicy.installmentFormula(),
                installmentPolicy.interestComponentFormula(),
                installmentPolicy.paymentType());
    }

    default DefineTradeLoanArrangementCommand.GracePeriodPolicyDto mapGracePeriodPolicy(
            DefineTradeLoanArrangementRequest.GracePeriodPolicyDto gracePeriodPolicy) {
        if (gracePeriodPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.GracePeriodPolicyDto(
                gracePeriodPolicy.minGracePeriodDays(),
                gracePeriodPolicy.maxGracePeriodDays(),
                gracePeriodPolicy.formula());
    }

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

    default DefineTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto mapRegulatoryCompliancePolicy(
            DefineTradeLoanArrangementRequest.RegulatoryCompliancePolicyDto regulatoryCompliancePolicy) {
        if (regulatoryCompliancePolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto(
                regulatoryCompliancePolicy.overDuePeriodMonths(),
                regulatoryCompliancePolicy.deferralPeriodMonths(),
                regulatoryCompliancePolicy.suspiciousPeriodMonths());
    }

    default DefineTradeLoanArrangementCommand.CollateralPolicyDto mapCollateralPolicy(
            DefineTradeLoanArrangementRequest.CollateralPolicyDto collateralPolicy) {
        if (collateralPolicy == null) return null;
        return new DefineTradeLoanArrangementCommand.CollateralPolicyDto(
                collateralPolicy.totalPercent(),
                collateralPolicy.collateralTypes().stream()
                        .map(code ->
                                new DefineTradeLoanArrangementCommand.CollateralTypeDto(CollateralType.valueOf(code)))
                        .collect(Collectors.toSet()),
                collateralPolicy.collateralCalculationType());
    }
}
