package ir.dotin.loan.trade.core.application.service.defineloanarrangement.mapper;

import java.time.Period;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Range;
import org.jspecify.annotations.NonNull;
import org.mapstruct.*;
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
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;
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
    @Mapping(target = "amountRange", ignore = true)
    public abstract TradeLoanArrangement.Builder toBuilder(DefineTradeLoanArrangementCommand command);

    abstract LoanArrangementCode map(DefineTradeLoanArrangementCommand.LoanArrangementCodeDto dto);

    abstract Title map(DefineTradeLoanArrangementCommand.TitleDto dto);

    abstract ConfirmType map(DefineTradeLoanArrangementCommand.ConfirmTypeDto dto);

    @AfterMapping
    protected void fillCurrency(
            @MappingTarget TradeLoanArrangement.Builder builder, DefineTradeLoanArrangementCommand command) {
        CurrencyType currency = map(command.currencyType());
        if (Objects.nonNull(command.amountRange())) {
            Money min =
                    Money.valueOf(command.amountRange().min().value(), currency).getValue();
            Money max =
                    Money.valueOf(command.amountRange().max().value(), currency).getValue();
            builder.amountRange(Range.closed(min, max));
        }
    }

    Range<@NonNull LoanDuration> mapDurationRange(DefineTradeLoanArrangementCommand.LoanDurationRangeDto dto) {
        return Range.closed(map(dto.min()), map(dto.max()));
    }

    LoanDuration map(Period period) {
        return LoanDuration.of(period).getValue();
    }

    InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> mapInterestPolicy(
            DefineTradeLoanArrangementCommand.InterestPolicyDto dto) {

        Rate minRate = Rate.valueOf(dto.minRate()).orElseThrow();
        Rate maxRate = Rate.valueOf(dto.maxRate()).orElseThrow();
        Formula interestFormula = Formula.valueOf(dto.interestFormula()).orElseThrow();
        Formula refundFormula = Formula.valueOf(dto.refundFormula()).orElseThrow();

        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> interestParam =
                createParameterizedFormula(interestFormula);
        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> refundParam =
                createParameterizedFormula(refundFormula);

        Range<@NonNull Rate> preferentialRange = Range.closed(minRate, maxRate);
        return InterestPolicy.of(minRate, preferentialRange, interestParam, refundParam, dto.dailyInterest())
                .orElseThrow();
    }

    PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> mapPenaltyPolicy(
            DefineTradeLoanArrangementCommand.PenaltyPolicyDto dto) {
        Rate penaltyRate = Rate.valueOf(dto.penaltyRate()).orElseThrow();
        Rate deferralRate = Rate.valueOf(dto.deferralInterestRate()).orElseThrow();
        Formula formula = Formula.valueOf(dto.formula()).orElseThrow();

        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> penaltyFormula =
                createParameterizedFormula(formula);

        return PenaltyPolicy.of(penaltyRate, deferralRate, penaltyFormula, dto.paymentType())
                .orElseThrow();
    }

    InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> mapInstallmentPolicy(
            DefineTradeLoanArrangementCommand.InstallmentPolicyDto dto) {
        InstallmentPeriod period = InstallmentPeriod.of(dto.installmentPeriod()).orElseThrow();
        Formula installmentFormula = Formula.valueOf(dto.installmentFormula()).orElseThrow();
        Formula interestComponentFormula =
                Formula.valueOf(dto.interestComponentFormula()).orElseThrow();

        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> installmentParam =
                createParameterizedFormula(installmentFormula);
        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> interestParam =
                createParameterizedFormula(interestComponentFormula);

        return InstallmentPolicy.of(period, installmentParam, interestParam, dto.paymentType())
                .orElseThrow();
    }

    GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> mapGracePeriodPolicy(
            DefineTradeLoanArrangementCommand.GracePeriodPolicyDto dto) {
        Period minPeriod = Period.ofDays(dto.minGracePeriodDays());
        Period maxPeriod = Period.ofDays(dto.maxGracePeriodDays());
        Formula formula = Formula.valueOf(dto.formula()).orElseThrow();

        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> gracePeriodFormula =
                createParameterizedFormula(formula);

        return GracePeriodPolicy.of(minPeriod, maxPeriod, gracePeriodFormula).orElseThrow();
    }

    RepaymentPriorityPolicy mapRepaymentPriorityPolicy(
            DefineTradeLoanArrangementCommand.RepaymentPriorityPolicyDto dto) {
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
            DefineTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto dto) {
        return RegulatoryCompliancePolicy.of(dto.overDuePeriod(), dto.deferralPeriod(), dto.suspiciousPeriod())
                .orElseThrow();
    }

    CollateralPolicy mapCollateralPolicy(DefineTradeLoanArrangementCommand.CollateralPolicyDto dto) {
        List<CollateralType> types = dto.collateralTypes().stream()
                .map(ct -> CollateralType.of(ct.code()).orElseThrow())
                .toList();
        return CollateralPolicy.of(types, dto.totalPercent(), dto.collateralCalculationType())
                .orElseThrow();
    }

    LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>
            createParameterizedFormula(Formula formula) {
        return LoanFacilityParameterizedFormula.valueOf(formula, tradeLoanFormulaFieldMappingProperties.fieldMap())
                .orElseThrow();
    }

    public abstract EconomicSector map(DefineTradeLoanArrangementCommand.EconomicSectorDto dto);

    abstract CurrencyType map(DefineTradeLoanArrangementCommand.CurrencyTypeDto dto);
}
