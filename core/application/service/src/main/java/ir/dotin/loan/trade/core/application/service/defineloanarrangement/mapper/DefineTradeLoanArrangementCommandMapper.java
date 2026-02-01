package ir.dotin.loan.trade.core.application.service.defineloanarrangement.mapper;

import java.time.Period;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Range;
import org.jspecify.annotations.NonNull;
import org.mapstruct.*;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.commons.domain.vo.Rate;
import ir.dotin.platform.formula.api.FormulaId;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.CollateralPolicy;
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
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.CurrencyTypeDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.EconomicSectorDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.LoanArrangementCodeDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.TitleDto;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

@Mapper(config = BaseMapperConfig.class)
public abstract class DefineTradeLoanArrangementCommandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "disable", ignore = true)
    @Mapping(target = "previousVersion", ignore = true)
    @Mapping(target = "amountRange", ignore = true)
    public abstract TradeLoanArrangement.Builder toBuilder(DefineTradeLoanArrangementCommand command);

    abstract LoanArrangementCode map(LoanArrangementCodeDto dto);

    abstract Title map(TitleDto dto);

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

    InterestPolicy mapInterestPolicy(DefineTradeLoanArrangementCommand.InterestPolicyDto dto) {

        Rate minRate = Rate.valueOf(dto.minPreferentialRate()).orElseThrow();
        Rate maxRate = Rate.valueOf(dto.maxPreferentialRate()).orElseThrow();
        FormulaId interestFormula = FormulaId.of(dto.interestFormula());
        FormulaId refundFormula = FormulaId.of(dto.refundFormula());
        Range<@NonNull Rate> preferentialRange = Range.closed(minRate, maxRate);
        return InterestPolicy.of(
                        Rate.valueOf(dto.rate()).orElseThrow(),
                        preferentialRange,
                        interestFormula,
                        refundFormula,
                        dto.dailyInterest())
                .orElseThrow();
    }

    PenaltyPolicy mapPenaltyPolicy(DefineTradeLoanArrangementCommand.PenaltyPolicyDto dto) {
        Rate penaltyRate = Rate.valueOf(dto.penaltyRate()).orElseThrow();
        Rate deferralRate = Rate.valueOf(dto.deferralInterestRate()).orElseThrow();
        FormulaId formula = FormulaId.of(dto.formula());

        return PenaltyPolicy.of(penaltyRate, deferralRate, formula, dto.paymentType())
                .orElseThrow();
    }

    InstallmentPolicy mapInstallmentPolicy(DefineTradeLoanArrangementCommand.InstallmentPolicyDto dto) {
        InstallmentPeriod period = InstallmentPeriod.of(dto.installmentPeriod()).orElseThrow();
        FormulaId installmentFormula = FormulaId.of(dto.installmentFormula());
        FormulaId interestComponentFormula = FormulaId.of(dto.interestComponentFormula());

        return InstallmentPolicy.of(period, installmentFormula, interestComponentFormula, dto.paymentType())
                .orElseThrow();
    }

    GracePeriodPolicy mapGracePeriodPolicy(DefineTradeLoanArrangementCommand.GracePeriodPolicyDto dto) {
        Period minPeriod = Period.ofDays(dto.minGracePeriodDays());
        Period maxPeriod = Period.ofDays(dto.maxGracePeriodDays());
        FormulaId formula = FormulaId.of(dto.formula());

        return GracePeriodPolicy.of(minPeriod, maxPeriod, formula).orElseThrow();
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
                .map(ct -> CollateralType.valueOf(ct.type().name()))
                .toList();
        return CollateralPolicy.of(types, dto.totalPercent(), dto.collateralCalculationType())
                .orElseThrow();
    }

    public abstract EconomicSector map(EconomicSectorDto dto);

    abstract CurrencyType map(CurrencyTypeDto dto);
}
