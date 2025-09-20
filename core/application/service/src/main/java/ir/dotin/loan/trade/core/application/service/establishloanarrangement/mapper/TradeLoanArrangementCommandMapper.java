package ir.dotin.loan.trade.core.application.service.establishloanarrangement.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.convert.utils.TypeDescriptors;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.CollateralPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.GracePeriodPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InterestPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.PenaltyPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RegulatoryCompliancePolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RepaymentPriorityPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.service.configuration.TradeFeatureProperties;
import ir.dotin.loan.trade.core.application.service.configuration.TradeLoanFormulaFieldMappingProperties;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

import static java.util.Objects.requireNonNull;

@Component
public class TradeLoanArrangementCommandMapper
        implements Converter<EstablishTradeLoanArrangementCommand, Result<TradeLoanArrangement.Builder>> {

    private static final Logger log = LoggerFactory.getLogger(TradeLoanArrangementCommandMapper.class);
    private final TradeFeatureProperties featureProperties;
    private final TradeLoanFormulaFieldMappingProperties formulaFieldMappingProperties;
    private final ConversionService conversionService;

    public TradeLoanArrangementCommandMapper(
            TradeFeatureProperties featureProperties,
            TradeLoanFormulaFieldMappingProperties formulaFieldMappingProperties,
            ConversionService conversionService) {
        this.featureProperties = featureProperties;
        this.formulaFieldMappingProperties = formulaFieldMappingProperties;
        this.conversionService = conversionService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<TradeLoanArrangement.Builder> convert(@NonNull EstablishTradeLoanArrangementCommand command) {
        log.debug("Converting EstablishTradeLoanArrangementCommand to TradeLoanArrangement.Builder");

        Notification aggregatedNotification = Notification.create();
        TradeLoanArrangement.Builder builder = TradeLoanArrangement.newBuilder(featureProperties.featureConfig());

        Result<LoanArrangementCode> codeResult = convertCode(command.code());
        if (codeResult.isFailure()) {
            aggregatedNotification.merge(codeResult.notification());
        } else {
            builder.withCode(codeResult.value());
        }

        Result<Title> titleResult = convertTitle(command.title());
        if (titleResult.isFailure()) {
            aggregatedNotification.merge(titleResult.notification());
        } else {
            builder.withTitle(titleResult.value());
        }

        Result<Set<CurrencyType>> currenciesResult = convertCurrencies(command.currencies());
        if (currenciesResult.isFailure()) {
            aggregatedNotification.merge(currenciesResult.notification());
        } else {
            builder.withCurrencies(currenciesResult.value());
        }

        builder.withAmountRange(command.amountRange())
                .withDurationRange(command.durationRange())
                .withGuarantorCount(command.guarantorCount())
                .withCustomerType(command.customerType())
                .withHasInstallmentCard(command.hasInstallmentCard())
                .withLifeInsurancePaymentType(command.lifeInsurancePaymentType())
                .withLoanSecondaryType(command.loanSecondaryType())
                .withSectionType(command.sectionType())
                .withAutoApproval(command.autoApproval())
                .withDisbursementMethod(command.disbursementMethod());

        Result<InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>> interestPolicyResult =
                (Result<InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>)
                        conversionService.convert(
                                command.interestPolicy(),
                                TypeDescriptors.of(EstablishTradeLoanArrangementCommand.InterestPolicyDto.class),
                                TypeDescriptors.resultOf(InterestPolicy.class));
        if (requireNonNull(interestPolicyResult).isFailure()) {
            aggregatedNotification.merge(interestPolicyResult.notification());
        } else {
            builder.withInterestPolicy(interestPolicyResult.value());
        }

        Result<PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>> penaltyPolicyResult =
                (Result<PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>)
                        conversionService.convert(
                                command.penaltyPolicy(),
                                TypeDescriptors.of(EstablishTradeLoanArrangementCommand.PenaltyPolicyDto.class),
                                TypeDescriptors.resultOf(PenaltyPolicy.class));
        if (requireNonNull(penaltyPolicyResult).isFailure()) {
            aggregatedNotification.merge(penaltyPolicyResult.notification());
        } else {
            builder.withPenaltyPolicy(penaltyPolicyResult.value());
        }

        Result<InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>> installmentPolicyResult =
                (Result<InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>)
                        conversionService.convert(
                                command.installmentPolicy(),
                                TypeDescriptors.of(EstablishTradeLoanArrangementCommand.InstallmentPolicyDto.class),
                                TypeDescriptors.resultOf(InstallmentPolicy.class));
        if (requireNonNull(installmentPolicyResult).isFailure()) {
            aggregatedNotification.merge(installmentPolicyResult.notification());
        } else {
            builder.withInstallmentPolicy(installmentPolicyResult.value());
        }

        Result<GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>> gracePeriodPolicyResult =
                (Result<GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>)
                        conversionService.convert(
                                command.gracePeriodPolicy(),
                                TypeDescriptors.of(EstablishTradeLoanArrangementCommand.GracePeriodPolicyDto.class),
                                TypeDescriptors.resultOf(GracePeriodPolicy.class));
        if (requireNonNull(gracePeriodPolicyResult).isFailure()) {
            aggregatedNotification.merge(gracePeriodPolicyResult.notification());
        } else {
            builder.withGracePeriodPolicy(gracePeriodPolicyResult.value());
        }

        Result<RepaymentPriorityPolicy> repaymentPriorityPolicyResult =
                (Result<RepaymentPriorityPolicy>) conversionService.convert(
                        command.repaymentPriorityPolicy(),
                        TypeDescriptors.of(EstablishTradeLoanArrangementCommand.RepaymentPriorityPolicyDto.class),
                        TypeDescriptors.resultOf(RepaymentPriorityPolicy.class));
        if (requireNonNull(repaymentPriorityPolicyResult).isFailure()) {
            aggregatedNotification.merge(repaymentPriorityPolicyResult.notification());
        } else {
            builder.withRepaymentPriorityPolicy(repaymentPriorityPolicyResult.value());
        }

        Result<RegulatoryCompliancePolicy> regulatoryCompliancePolicyResult =
                (Result<RegulatoryCompliancePolicy>) conversionService.convert(
                        command.regulatoryCompliancePolicy(),
                        TypeDescriptors.of(EstablishTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto.class),
                        TypeDescriptors.resultOf(RegulatoryCompliancePolicy.class));
        if (requireNonNull(regulatoryCompliancePolicyResult).isFailure()) {
            aggregatedNotification.merge(regulatoryCompliancePolicyResult.notification());
        } else {
            builder.withRegulatoryCompliancePolicy(regulatoryCompliancePolicyResult.value());
        }

        Result<CollateralPolicy> collateralPolicyResult = (Result<CollateralPolicy>) conversionService.convert(
                command.collateralPolicy(),
                TypeDescriptors.of(EstablishTradeLoanArrangementCommand.CollateralPolicyDto.class),
                TypeDescriptors.resultOf(CollateralPolicy.class));
        if (requireNonNull(collateralPolicyResult).isFailure()) {
            aggregatedNotification.merge(collateralPolicyResult.notification());
        } else {
            builder.withCollateralPolicy(collateralPolicyResult.value());
        }

        if (aggregatedNotification.hasErrors()) {
            log.warn("Conversion failed with errors: {}", aggregatedNotification.getErrorMessages());
            return Result.failure(aggregatedNotification);
        }

        log.debug("Successfully converted EstablishTradeLoanArrangementCommand to TradeLoanArrangement.Builder");
        return Result.success(builder);
    }

    private Result<LoanArrangementCode> convertCode(String code) {
        return LoanArrangementCode.valueOf(code);
    }

    private Result<Title> convertTitle(String title) {
        return Title.of(title);
    }

    private Result<Set<CurrencyType>> convertCurrencies(Set<String> currencyCodes) {
        Set<CurrencyType> currencies = currencyCodes.stream()
                .map(code -> {
                    Result<CurrencyType> currencyResult = CurrencyType.valueOf(code);
                    return currencyResult.orElseThrow();
                })
                .collect(Collectors.toSet());

        return Result.success(currencies);
    }
}
