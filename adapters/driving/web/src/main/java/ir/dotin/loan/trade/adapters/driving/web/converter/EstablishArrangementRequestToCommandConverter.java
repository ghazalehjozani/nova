package ir.dotin.loan.trade.adapters.driving.web.converter;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.web.controller.command.dto.*;
import ir.dotin.loan.trade.adapters.driving.web.controller.command.dto.EstablishArrangementRequest;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;

@Component
public class EstablishArrangementRequestToCommandConverter
        implements Converter<EstablishArrangementRequest, EstablishTradeLoanArrangementCommand> {

    @Override
    public EstablishTradeLoanArrangementCommand convert(EstablishArrangementRequest source) {
        return new EstablishTradeLoanArrangementCommand(
                source.getCode(),
                source.getTitle(),
                source.getCurrencies(),
                source.getAmountRange().getMinAmount(),
                source.getAmountRange().getMaxAmount(),
                Duration.ofDays(source.getDurationRange().getMinDurationDays()),
                Duration.ofDays(source.getDurationRange().getMaxDurationDays()),
                source.getPartyType().name(),
                source.getGuarantorCount(),
                source.getConfirmationRequirement() != null
                        ? source.getConfirmationRequirement().getConfirmerRole()
                        : null,
                source.getDisbursementMethod().name(),
                convertInterestPolicy(source.getInterestPolicy()),
                convertPenaltyPolicy(source.getPenaltyPolicy()),
                convertInstallmentPolicy(source.getInstallmentPolicy()),
                convertGracePeriodPolicy(source.getGracePeriodPolicy()),
                convertRepaymentPriorityPolicy(source.getRepaymentPriorityPolicy()),
                convertRegulatoryCompliancePolicy(source.getRegulatoryCompliancePolicy()),
                convertCollateralPolicy(source.getCollateralPolicy()),
                source.getLifeInsurancePaymentType().name(),
                source.getLoanSecondaryType().name(),
                source.getSectionType().name(),
                source.isHasInstallmentCard(),
                source.isAutoApproval(),
                UUID.randomUUID());
    }

    private EstablishTradeLoanArrangementCommand.InterestPolicyDto convertInterestPolicy(InterestPolicyDto source) {
        return new EstablishTradeLoanArrangementCommand.InterestPolicyDto(
                source.getBaseInterestRate(),
                source.getPreferentialRateRange().getMinRate(),
                source.getPreferentialRateRange().getMaxRate(),
                new EstablishTradeLoanArrangementCommand.FormulaDto(
                        source.getInterestFormula(), convertFieldMappings(source.getFieldMappings())),
                new EstablishTradeLoanArrangementCommand.FormulaDto(
                        source.getRefundInterestFormula(), convertFieldMappings(source.getFieldMappings())),
                source.isDailyInterest());
    }

    private EstablishTradeLoanArrangementCommand.PenaltyPolicyDto convertPenaltyPolicy(PenaltyPolicyDto source) {
        return new EstablishTradeLoanArrangementCommand.PenaltyPolicyDto(
                source.getPenaltyRate(),
                source.getDeferralInterestRate(),
                new EstablishTradeLoanArrangementCommand.FormulaDto(source.getPenaltyFormula(), Map.of()),
                source.getPenaltyPaymentType().name());
    }

    private EstablishTradeLoanArrangementCommand.InstallmentPolicyDto convertInstallmentPolicy(
            InstallmentPolicyDto source) {
        return new EstablishTradeLoanArrangementCommand.InstallmentPolicyDto(
                source.getInstallmentPeriodDays(),
                new EstablishTradeLoanArrangementCommand.FormulaDto(source.getInstallmentFormula(), Map.of()),
                new EstablishTradeLoanArrangementCommand.FormulaDto(source.getInterestComponentFormula(), Map.of()),
                source.getInstallmentPaymentType().name(),
                source.isDefineAutomaticInstallment());
    }

    private EstablishTradeLoanArrangementCommand.GracePeriodPolicyDto convertGracePeriodPolicy(
            GracePeriodPolicyDto source) {
        return new EstablishTradeLoanArrangementCommand.GracePeriodPolicyDto(
                source.getMinGracePeriodDays(),
                source.getMaxGracePeriodDays(),
                new EstablishTradeLoanArrangementCommand.FormulaDto(source.getGracePeriodFormula(), Map.of()));
    }

    private EstablishTradeLoanArrangementCommand.RepaymentPriorityPolicyDto convertRepaymentPriorityPolicy(
            RepaymentPriorityPolicyDto source) {
        return new EstablishTradeLoanArrangementCommand.RepaymentPriorityPolicyDto(
                source.getInstallmentMainAmountPriority(),
                source.getInstallmentInterestAmountPriority(),
                source.getInstallmentPenaltyAmountPriority(),
                source.getInstallmentIncomeAmountPriority(),
                source.getInsuranceAmountPriority(),
                source.getInsurancePenaltyAmountPriority(),
                source.isHasEqualPriority());
    }

    private EstablishTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto convertRegulatoryCompliancePolicy(
            RegulatoryCompliancePolicyDto source) {
        return new EstablishTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto(
                source.getOverDuePeriodDays(), source.getDeferralPeriodDays(), source.getSuspiciousPeriodDays());
    }

    private EstablishTradeLoanArrangementCommand.CollateralPolicyDto convertCollateralPolicy(
            CollateralPolicyDto source) {
        return new EstablishTradeLoanArrangementCommand.CollateralPolicyDto(
                source.getCollateralTypes().stream()
                        .map(type -> new EstablishTradeLoanArrangementCommand.CollateralTypeDto(
                                type.getCode(), type.getName()))
                        .collect(java.util.stream.Collectors.toSet()),
                source.getTotalPercent());
    }

    private Map<String, String> convertFieldMappings(Map<Character, FormulaFieldDto> fieldMappings) {
        if (fieldMappings == null) {
            return Map.of();
        }
        return fieldMappings.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()), e -> e.getValue().getFieldName()));
    }
}
