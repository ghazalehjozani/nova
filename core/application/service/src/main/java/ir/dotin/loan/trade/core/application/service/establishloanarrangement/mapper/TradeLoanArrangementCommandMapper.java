package ir.dotin.loan.trade.core.application.service.establishloanarrangement.mapper;

import java.math.BigDecimal;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Range;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.core.feature.FeatureConfig;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Formula;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.commons.domain.vo.ParameterizedFormula;
import ir.dotin.platform.commons.domain.vo.Rate;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
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
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PenaltyPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Disable;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityParameterizedFormula;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand.CollateralPolicyDto;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand.FormulaDto;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand.GracePeriodPolicyDto;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand.InstallmentPolicyDto;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand.InterestPolicyDto;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand.PenaltyPolicyDto;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand.RepaymentPriorityPolicyDto;
import ir.dotin.loan.trade.core.application.service.establishloanarrangement.i18n.LoanArrangementErrorCodes;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Component
public class TradeLoanArrangementCommandMapper
        implements Converter<EstablishTradeLoanArrangementCommand, Result<TradeLoanArrangement.Builder>> {

    private static final Logger log = LoggerFactory.getLogger(TradeLoanArrangementCommandMapper.class);

    public Result<TradeLoanArrangement.Builder> convert(@NonNull EstablishTradeLoanArrangementCommand command) {
        Notification notification = Notification.create();
        try {
            TradeLoanArrangement.Builder builder = TradeLoanArrangement.newBuilder(
                    new FeatureConfig(Map.of("123", false))); // TODO: Remove feature config
            // Core field mappings
            mapCoreFields(command, builder, notification);

            // Policy mappings
            mapPolicies(command, builder, notification);

            // Flag mappings
            builder.withHasInstallmentCard(command.hasInstallmentCard()).withAutoApproval(command.autoApproval());

            return notification.hasErrors() ? Result.failure(notification) : Result.success(builder);
        } catch (Exception e) {
            log.error("Error mapping command to builder", e);
            notification.addError(LoanArrangementErrorCodes.MAPPING_ERROR, e.getMessage());
            return Result.failure(notification);
        }
    }

    private void mapCoreFields(
            EstablishTradeLoanArrangementCommand command,
            TradeLoanArrangement.Builder builder,
            Notification notification) {

        // Basic identification fields
        mapBasicFields(command, builder, notification);

        // Financial and temporal ranges
        mapCurrenciesAndRanges(command, builder, notification);

        // Enumerations and types
        mapEnumerations(command, builder, notification);

        // Optional configurations
        mapOptionalFields(command, builder, notification);
    }

    private void mapBasicFields(
            EstablishTradeLoanArrangementCommand command,
            TradeLoanArrangement.Builder builder,
            Notification notification) {

        // Code
        Result<LoanArrangementCode> codeResult = LoanArrangementCode.valueOf(command.code());
        if (codeResult.isFailure()) {
            notification.merge(codeResult.notification());
        } else {
            builder.withCode(codeResult.value());
        }

        // Title
        Result<Title> titleResult = Title.of(command.title());
        if (titleResult.isFailure()) {
            notification.merge(titleResult.notification());
        } else {
            builder.withTitle(titleResult.value());
        }

        // Set initial state
        builder.withActive(new Active(true)).withDisable(new Disable(false));
    }

    private void mapCurrenciesAndRanges(
            EstablishTradeLoanArrangementCommand command,
            TradeLoanArrangement.Builder builder,
            Notification notification) {

        // Map currencies with validation
        Set<CurrencyType> currencies = mapCurrencies(command.currencies(), notification);

        if (!currencies.isEmpty()) {
            builder.withCurrencies(currencies);

            // Create amount range with primary currency
            mapAmountRange(command, currencies.iterator().next(), builder, notification);
        }

        // Duration range
        builder.withDurationRange(Range.closed(command.minDuration(), command.maxDuration()));
    }

    private Set<CurrencyType> mapCurrencies(Set<String> currencyCodes, Notification notification) {
        return currencyCodes.stream()
                .map(code -> {
                    Result<CurrencyType> result = CurrencyType.valueOf(code);
                    if (result.isFailure()) {
                        notification.merge(result.notification());
                        return null;
                    }
                    return result.value();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void mapAmountRange(
            EstablishTradeLoanArrangementCommand command,
            CurrencyType currency,
            TradeLoanArrangement.Builder builder,
            Notification notification) {

        Result<Money> minMoneyResult = Money.valueOf(command.minAmount(), currency);
        Result<Money> maxMoneyResult = Money.valueOf(command.maxAmount(), currency);

        if (minMoneyResult.isSuccess() && maxMoneyResult.isSuccess()) {
            Range<Money> amountRange = Range.closed(minMoneyResult.value(), maxMoneyResult.value());
            builder.withAmountRange(amountRange);
        } else {
            notification.merge(minMoneyResult.notification());
            notification.merge(maxMoneyResult.notification());
        }
    }

    private void mapEnumerations(
            EstablishTradeLoanArrangementCommand command,
            TradeLoanArrangement.Builder builder,
            Notification notification) {

        // Enum mappings with error handling
        mapEnumSafe(command.partyType(), PartyType::valueOf, builder::withCustomerType, notification);
        mapEnumSafe(
                command.disbursementMethod(),
                this::mapDisbursementMethod,
                builder::withDisbursementMethod,
                notification);
        mapEnumSafe(
                command.lifeInsurancePaymentType(),
                LifeInsurancePaymentType::valueOf,
                builder::withLifeInsurancePaymentType,
                notification);
        mapEnumSafe(
                command.loanSecondaryType(), this::mapLoanSecondaryType, builder::withLoanSecondaryType, notification);
        mapEnumSafe(command.sectionType(), this::mapSectionType, builder::withSectionType, notification);
    }

    /** Safe enum mapping helper */
    private <E> void mapEnumSafe(
            String value, Function<String, E> enumFactory, Consumer<E> setter, Notification notification) {
        try {
            setter.accept(enumFactory.apply(value));
        } catch (IllegalArgumentException e) {
            notification.addError(
                    LoanArrangementErrorCodes.MAPPING_ERROR, "Invalid enum value '" + value + "': " + e.getMessage());
        }
    }

    private void mapOptionalFields(
            EstablishTradeLoanArrangementCommand command,
            TradeLoanArrangement.Builder builder,
            Notification notification) {

        // Guarantor count
        if (command.guarantorCount() != null && command.guarantorCount() > 0) {
            builder.withGuarantorCount(command.guarantorCount());
        }

        // Confirm type
        if (command.confirmType() != null && !command.confirmType().isBlank()) {
            // Parse confirm type - assuming format "code:name"
            String[] parts = command.confirmType().split(":");
            if (parts.length == 2) {
                Result<ConfirmType> confirmTypeResult = ConfirmType.of(parts[0].trim(), parts[1].trim());
                if (confirmTypeResult.isSuccess()) {
                    builder.withConfirmType(confirmTypeResult.value());
                } else {
                    notification.merge(confirmTypeResult.notification());
                }
            }
        }
    }

    private void mapPolicies(
            EstablishTradeLoanArrangementCommand command,
            TradeLoanArrangement.Builder builder,
            Notification notification) {

        // Policy mappings with error handling
        mapPolicyAndApply(command.interestPolicy(), this::mapInterestPolicy, builder::withInterestPolicy, notification);

        mapPolicyAndApply(command.penaltyPolicy(), this::mapPenaltyPolicy, builder::withPenaltyPolicy, notification);

        mapPolicyAndApply(
                command.installmentPolicy(), this::mapInstallmentPolicy, builder::withInstallmentPolicy, notification);

        mapPolicyAndApply(
                command.gracePeriodPolicy(), this::mapGracePeriodPolicy, builder::withGracePeriodPolicy, notification);

        mapPolicyAndApply(
                command.repaymentPriorityPolicy(),
                this::mapRepaymentPriorityPolicy,
                builder::withRepaymentPriorityPolicy,
                notification);

        mapPolicyAndApply(
                command.regulatoryCompliancePolicy(),
                this::mapRegulatoryCompliancePolicy,
                builder::withRegulatoryCompliancePolicy,
                notification);

        mapPolicyAndApply(
                command.collateralPolicy(), this::mapCollateralPolicy, builder::withCollateralPolicy, notification);
    }

    /** Generic policy mapping helper to reduce code duplication */
    private <T, R> void mapPolicyAndApply(
            T dto, Function<T, Result<R>> mapper, Consumer<R> setter, Notification notification) {
        try {
            Result<R> result = mapper.apply(dto);
            if (result.isSuccess()) {
                setter.accept(result.value());
            } else {
                notification.merge(result.notification());
            }
        } catch (Exception e) {
            notification.addError(LoanArrangementErrorCodes.MAPPING_ERROR, "Failed to map policy: " + e.getMessage());
        }
    }

    private Result<InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>> mapInterestPolicy(
            InterestPolicyDto dto) {
        try {
            Rate baseRate = safeCreateRate(dto.baseInterestRate());
            Range<Rate> preferentialRange = createRateRange(dto.minPreferentialRate(), dto.maxPreferentialRate());

            var interestFormula =
                    createLoanFacilityFormula(dto.interestFormula()).orElseThrow();
            var refundFormula =
                    createLoanFacilityFormula(dto.refundInterestFormula()).orElseThrow();

            return InterestPolicy.of(baseRate, preferentialRange, interestFormula, refundFormula, dto.dailyInterest());
        } catch (Exception e) {
            return createMappingError("interest policy", e);
        }
    }

    private Result<PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>> mapPenaltyPolicy(
            PenaltyPolicyDto dto) {
        try {
            return PenaltyPolicy.of(
                    safeCreateRate(dto.penaltyRate()),
                    safeCreateRate(dto.deferralInterestRate()),
                    createParameterizedFormula(dto.penaltyFormula()).orElseThrow(),
                    mapPenaltyPaymentType(dto.penaltyPaymentType()));
        } catch (Exception e) {
            return createMappingError("penalty policy", e);
        }
    }

    private Result<InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>> mapInstallmentPolicy(
            InstallmentPolicyDto dto) {
        try {
            InstallmentPeriod installmentPeriod = InstallmentPeriod.of(Period.ofDays(dto.installmentPeriodDays()))
                    .orElseThrow();

            return InstallmentPolicy.of(
                    installmentPeriod,
                    createParameterizedFormula(dto.installmentFormula()).orElseThrow(),
                    createParameterizedFormula(dto.interestComponentFormula()).orElseThrow(),
                    mapInstallmentPaymentType(dto.installmentPaymentType()),
                    dto.isDefineAutomaticInstallment());
        } catch (Exception e) {
            return createMappingError("installment policy", e);
        }
    }

    private Result<GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>> mapGracePeriodPolicy(
            GracePeriodPolicyDto dto) {
        try {
            return GracePeriodPolicy.of(
                    Period.ofDays(dto.minGracePeriodDays()),
                    Period.ofDays(dto.maxGracePeriodDays()),
                    createParameterizedFormula(dto.gracePeriodFormula()).orElseThrow());
        } catch (Exception e) {
            return createMappingError("grace period policy", e);
        }
    }

    private Result<RepaymentPriorityPolicy> mapRepaymentPriorityPolicy(RepaymentPriorityPolicyDto dto) {
        try {
            return dto.hasEqualPriority()
                    ? RepaymentPriorityPolicy.ofEqualPriorities()
                    : RepaymentPriorityPolicy.of(
                            dto.installmentMainAmountPriority(),
                            dto.installmentInterestAmountPriority(),
                            dto.installmentPenaltyAmountPriority(),
                            dto.installmentIncomeAmountPriority(),
                            dto.insuranceAmountPriority(),
                            dto.insurancePenaltyAmountPriority(),
                            dto.hasEqualPriority());
        } catch (Exception e) {
            return createMappingError("repayment priority policy", e);
        }
    }

    private Result<RegulatoryCompliancePolicy> mapRegulatoryCompliancePolicy(RegulatoryCompliancePolicyDto dto) {
        try {
            return Result.success(
                    RegulatoryCompliancePolicy.of(dto.overDuePeriod(), dto.deferralPeriod(), dto.suspiciousPeriod())
                            .orElseThrow());
        } catch (Exception e) {
            return createMappingError("regulatory compliance policy", e);
        }
    }

    private Result<CollateralPolicy> mapCollateralPolicy(CollateralPolicyDto dto) {
        try {
            Set<CollateralType> types = dto.collateralTypes().stream()
                    .map(typeDto ->
                            CollateralType.of(typeDto.code(), typeDto.name()).orElseThrow())
                    .collect(Collectors.toSet());

            return Result.success(CollateralPolicy.of(types, dto.totalPercent()).orElseThrow());
        } catch (Exception e) {
            return createMappingError("collateral policy", e);
        }
    }

    private Result<LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>
            createLoanFacilityFormula(FormulaDto dto) {
        return createFormula(dto, true);
    }

    private Result<ParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>
            createParameterizedFormula(FormulaDto dto) {
        return createFormula(dto, false);
    }

    /** Unified formula creation method to eliminate duplication */
    private <T> Result<T> createFormula(FormulaDto dto, boolean asLoanFacilityFormula) {
        try {
            Formula formula = safeCreateFormula(dto.expression());
            Map<Character, TradeLoanFacilityFormulaField> fieldMappings = mapFieldMappings(dto.fieldMappings());
            ParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> paramFormula =
                    ParameterizedFormula.valueOf(formula, fieldMappings).orElseThrow();

            if (asLoanFacilityFormula) {
                return (Result<T>) LoanFacilityParameterizedFormula.valueOf(paramFormula);
            } else {
                return (Result<T>) Result.success(paramFormula);
            }
        } catch (Exception e) {
            return Result.failure(Notification.ofError(
                    LoanArrangementErrorCodes.MAPPING_ERROR, "Failed to create formula: " + e.getMessage()));
        }
    }

    private Formula safeCreateFormula(String expression) {
        return Formula.valueOf(expression)
                .orElseThrow(() -> new IllegalArgumentException("Invalid formula expression: " + expression));
    }

    private Map<Character, TradeLoanFacilityFormulaField> mapFieldMappings(Map<String, String> stringMappings) {
        Map<Character, TradeLoanFacilityFormulaField> fieldMappings = new HashMap<>();

        if (stringMappings == null || stringMappings.isEmpty()) {
            return getDefaultFieldMappings();
        }

        for (Map.Entry<String, String> entry : stringMappings.entrySet()) {
            if (entry.getKey().length() != 1) {
                log.warn("Invalid variable name '{}' - must be single character", entry.getKey());
                continue;
            }

            char variable = entry.getKey().charAt(0);
            String fieldName = entry.getValue().toUpperCase();

            try {
                TradeLoanFacilityFormulaField field = TradeLoanFacilityFormulaField.valueOf(fieldName);
                fieldMappings.put(variable, field);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown field name '{}' for variable '{}'", fieldName, variable);
                TradeLoanFacilityFormulaField defaultField = getDefaultFieldForVariable(variable);
                if (defaultField != null) {
                    fieldMappings.put(variable, defaultField);
                }
            }
        }

        return fieldMappings;
    }

    // Helper methods for common operations
    private Rate safeCreateRate(BigDecimal value) {
        return Rate.valueOf(value).orElseThrow(() -> new IllegalArgumentException("Invalid rate value: " + value));
    }

    private Range<Rate> createRateRange(BigDecimal min, BigDecimal max) {
        return Range.closed(safeCreateRate(min), safeCreateRate(max));
    }

    private <T> Result<T> createMappingError(String policyType, Exception e) {
        return Result.failure(Notification.ofError(
                LoanArrangementErrorCodes.MAPPING_ERROR, "Failed to map " + policyType + ": " + e.getMessage()));
    }

    private Map<Character, TradeLoanFacilityFormulaField> getDefaultFieldMappings() {
        Map<Character, TradeLoanFacilityFormulaField> defaults = new HashMap<>();
        defaults.put('p', TradeLoanFacilityFormulaField.APPROVED_AMOUNT);
        defaults.put('a', TradeLoanFacilityFormulaField.APPROVED_AMOUNT);
        defaults.put('r', TradeLoanFacilityFormulaField.REQUESTED_AMOUNT);
        defaults.put('t', TradeLoanFacilityFormulaField.REQUESTED_AMOUNT);
        defaults.put('c', TradeLoanFacilityFormulaField.COMMISSION_AMOUNT);
        defaults.put('s', TradeLoanFacilityFormulaField.SHIPMENT_VALUE);
        defaults.put('i', TradeLoanFacilityFormulaField.INSURANCE_RATE);
        return defaults;
    }

    private TradeLoanFacilityFormulaField getDefaultFieldForVariable(char variable) {
        return switch (Character.toLowerCase(variable)) {
            case 'p', 'a' -> TradeLoanFacilityFormulaField.APPROVED_AMOUNT;
            case 'r', 't' -> TradeLoanFacilityFormulaField.REQUESTED_AMOUNT;
            case 'c' -> TradeLoanFacilityFormulaField.COMMISSION_AMOUNT;
            case 's' -> TradeLoanFacilityFormulaField.SHIPMENT_VALUE;
            case 'i' -> TradeLoanFacilityFormulaField.INSURANCE_RATE;
            default -> null;
        };
    }

    // Helper methods for enum mapping from API values to domain values
    private DisbursementMethod mapDisbursementMethod(String value) {
        return switch (value) {
            case "LUMP_SUM" -> DisbursementMethod.LUMP_SUMP;
            case "STAGED_REGULAR" -> DisbursementMethod.STAGED_REGULAR;
            case "STAGED_IRREGULAR" -> DisbursementMethod.STAGED_IRREGULAR;
            default -> throw new IllegalArgumentException("Invalid disbursement method: " + value);
        };
    }

    private PenaltyPaymentType mapPenaltyPaymentType(String value) {
        return switch (value) {
            case "ADDED_TO_INSTALLMENT" -> PenaltyPaymentType.INSTALLMENT_PENALTY_PAYMENT;
            case "SEPARATE_PAYMENT", "DEDUCTED_FROM_PRINCIPAL" -> PenaltyPaymentType.SETTLEMENT_PENALTY_PAYMENT;
            default -> throw new IllegalArgumentException("Invalid penalty payment type: " + value);
        };
    }

    private InstallmentPaymentType mapInstallmentPaymentType(String value) {
        return switch (value) {
            case "EQUAL" -> InstallmentPaymentType.SCHEDULED;
            case "DECREASING" -> InstallmentPaymentType.GRADUAL;
            case "CUSTOM" -> InstallmentPaymentType.ONE_TIME;
            default -> throw new IllegalArgumentException("Invalid installment payment type: " + value);
        };
    }

    private LoanSecondaryType mapLoanSecondaryType(String value) {
        return switch (value) {
            case "WORKING_CAPITAL" -> LoanSecondaryType.GENERAL;
            case "FIXED_ASSETS" -> LoanSecondaryType.SPECIFIC;
            case "TRADE_FINANCE" -> LoanSecondaryType.GENERAL_AND_SPECIFIC;
            case "EXPORT_FINANCE" -> LoanSecondaryType.NONE;
            case "IMPORT_FINANCE" -> LoanSecondaryType.GENERAL;
            default -> throw new IllegalArgumentException("Invalid loan secondary type: " + value);
        };
    }

    private SectionType mapSectionType(String value) {
        return switch (value) {
            case "MANUFACTURING" -> SectionType.FIXED;
            case "SERVICES" -> SectionType.CURRENT;
            case "COMMERCE" -> SectionType.FIXED_AND_CURRENT;
            case "AGRICULTURE" -> SectionType.NONE;
            case "CONSTRUCTION" -> SectionType.FIXED;
            default -> throw new IllegalArgumentException("Invalid section type: " + value);
        };
    }
}
