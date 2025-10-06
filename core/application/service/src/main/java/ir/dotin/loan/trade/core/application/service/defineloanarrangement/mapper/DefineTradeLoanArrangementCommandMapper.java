package ir.dotin.loan.trade.core.application.service.defineloanarrangement.mapper;

import java.time.Period;
import java.util.List;

import com.google.common.collect.Range;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Formula;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.commons.domain.vo.Rate;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.CollateralPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.CollateralType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.GracePeriodPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPeriod;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InterestPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.PenaltyPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RegulatoryCompliancePolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RepaymentPriorityPolicy;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityParameterizedFormula;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;
import ir.dotin.loan.trade.core.application.service.configuration.TradeLoanFormulaFieldMappingProperties;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Mapper(config = BaseMapperConfig.class)
public abstract class DefineTradeLoanArrangementCommandMapper {

    @Autowired
    private TradeLoanFormulaFieldMappingProperties tradeLoanFormulaFieldMappingProperties;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "disable", ignore = true)
    @Mapping(target = "previousVersion", ignore = true)
    public abstract TradeLoanArrangement.Builder toBuilder(EstablishTradeLoanArrangementCommand command);

    abstract LoanArrangementCode map(EstablishTradeLoanArrangementCommand.LoanArrangementCodeDto dto);

    abstract Title map(EstablishTradeLoanArrangementCommand.TitleDto dto);

    abstract ConfirmType map(EstablishTradeLoanArrangementCommand.ConfirmTypeDto dto);

    Range<Money> map(EstablishTradeLoanArrangementCommand.AmountRangeDto dto) {
        return Range.closed(map(dto.min()), map(dto.max()));
    }

    abstract Money map(EstablishTradeLoanArrangementCommand.MoneyDto dto);

    Range<LoanDuration> mapDurationRange(EstablishTradeLoanArrangementCommand.LoanDurationRangeDto dto) {
        return Range.closed(map(dto.minDays()), map(dto.maxDays()));
    }

    LoanDuration map(Period period) {
        return LoanDuration.of(period).getValue();
    }

    InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> mapInterestPolicy(
            EstablishTradeLoanArrangementCommand.InterestPolicyDto dto) {
        Rate minRate = Rate.valueOf(dto.minRate()).orElseThrow();
        Rate maxRate = Rate.valueOf(dto.maxRate()).orElseThrow();
        Formula interestFormula = Formula.valueOf(dto.interestFormula()).orElseThrow();
        Formula refundFormula = Formula.valueOf(dto.refundFormula()).orElseThrow();

        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> interestParam =
                createParameterizedFormula(interestFormula);
        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> refundParam =
                createParameterizedFormula(refundFormula);

        Range<Rate> preferentialRange = Range.closed(minRate, maxRate);
        return InterestPolicy.of(minRate, preferentialRange, interestParam, refundParam, dto.dailyInterest())
                .orElseThrow();
    }

    PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> mapPenaltyPolicy(
            EstablishTradeLoanArrangementCommand.PenaltyPolicyDto dto) {
        Rate penaltyRate = Rate.valueOf(dto.penaltyRate()).orElseThrow();
        Rate deferralRate = Rate.valueOf(dto.deferralInterestRate()).orElseThrow();
        Formula formula = Formula.valueOf(dto.formula()).orElseThrow();

        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> penaltyFormula =
                createParameterizedFormula(formula);

        return PenaltyPolicy.of(penaltyRate, deferralRate, penaltyFormula, dto.paymentType())
                .orElseThrow();
    }

    InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> mapInstallmentPolicy(
            EstablishTradeLoanArrangementCommand.InstallmentPolicyDto dto) {
        InstallmentPeriod period = InstallmentPeriod.of(dto.installmentPeriod()).orElseThrow();
        Formula installmentFormula = Formula.valueOf(dto.installmentFormula()).orElseThrow();
        Formula interestComponentFormula =
                Formula.valueOf(dto.interestComponentFormula()).orElseThrow();

        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> installmentParam =
                createParameterizedFormula(installmentFormula);
        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> interestParam =
                createParameterizedFormula(interestComponentFormula);

        return InstallmentPolicy.of(
                        period, installmentParam, interestParam, dto.paymentType(), dto.isDefineAutomaticInstallment())
                .orElseThrow();
    }

    GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> mapGracePeriodPolicy(
            EstablishTradeLoanArrangementCommand.GracePeriodPolicyDto dto) {
        Period minPeriod = Period.ofDays(dto.minGracePeriodDays());
        Period maxPeriod = Period.ofDays(dto.maxGracePeriodDays());
        Formula formula = Formula.valueOf(dto.formula()).orElseThrow();

        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> gracePeriodFormula =
                createParameterizedFormula(formula);

        return GracePeriodPolicy.of(minPeriod, maxPeriod, gracePeriodFormula).orElseThrow();
    }

    RepaymentPriorityPolicy mapRepaymentPriorityPolicy(
            EstablishTradeLoanArrangementCommand.RepaymentPriorityPolicyDto dto) {
        return RepaymentPriorityPolicy.of(
                        dto.principalPriority(),
                        dto.interestPriority(),
                        dto.penaltyPriority(),
                        dto.commissionPriority(),
                        dto.insurancePriority(),
                        dto.insurancePenaltyPriority(),
                        dto.hasEqualPriority())
                .orElseThrow();
    }

    RegulatoryCompliancePolicy mapRegulatoryCompliancePolicy(
            EstablishTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto dto) {
        return RegulatoryCompliancePolicy.of(dto.overDuePeriod(), dto.deferralPeriod(), dto.suspiciousPeriod())
                .orElseThrow();
    }

    CollateralPolicy mapCollateralPolicy(EstablishTradeLoanArrangementCommand.CollateralPolicyDto dto) {
        List<CollateralType> types = dto.collateralTypes().stream()
                .map(ct -> CollateralType.of(ct.code(), ct.name()).orElseThrow())
                .toList();
        return CollateralPolicy.of(types, dto.totalPercent()).orElseThrow();
    }

    LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>
            createParameterizedFormula(Formula formula) {
        return LoanFacilityParameterizedFormula.valueOf(formula, tradeLoanFormulaFieldMappingProperties.fieldMap())
                .orElseThrow();
    }

    abstract EconomicSector map(EstablishTradeLoanArrangementCommand.EconomicSectorDto dto);

    abstract CurrencyType map(EstablishTradeLoanArrangementCommand.CurrencyTypeDto dto);
}
