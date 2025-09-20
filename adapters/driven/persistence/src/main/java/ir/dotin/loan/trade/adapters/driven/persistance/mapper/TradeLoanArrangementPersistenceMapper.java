package ir.dotin.loan.trade.adapters.driven.persistance.mapper;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Range;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.platform.adapter.persistence.embeddable.AmountRangeEmb;
import ir.dotin.platform.adapter.persistence.embeddable.DurationRangeEmb;
import ir.dotin.platform.commons.convert.utils.TypeDescriptors;
import ir.dotin.platform.commons.core.feature.FeatureConfig;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.CollateralPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.CollateralType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.GracePeriodPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InterestPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.PenaltyPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RegulatoryCompliancePolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RepaymentPriorityPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Disable;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.CollateralTypeEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.ConfirmTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.GracePeriodPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.InstallmentPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.InterestPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.PenaltyPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.RegulatoryCompliancePolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.RepaymentPriorityPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.InvalidDomainStateException;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.PersistenceConversionException;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@SuppressWarnings("unchecked")
@Component
public class TradeLoanArrangementPersistenceMapper {

    private final ConversionService conversionService;
    private final FeatureConfig featureConfig;

    public TradeLoanArrangementPersistenceMapper(
            @Lazy ConversionService conversionService, FeatureConfig featureConfig) {
        this.conversionService = conversionService;
        this.featureConfig = featureConfig;
    }

    @Transactional(readOnly = true)
    public TradeLoanArrangementEntity map(TradeLoanArrangement domain) {
        if (domain == null) return null;

        try {
            TradeLoanArrangementEntity entity = new TradeLoanArrangementEntity();

            // Map identity and basic fields
            mapBasicFieldsToEntity(domain, entity);

            // Map value objects
            mapValueObjectsToEntity(domain, entity);

            // Map policies
            mapPoliciesToEntity(domain, entity);

            // Map collateral
            mapCollateralToEntity(domain, entity);

            // Map optional fields
            mapOptionalFieldsToEntity(domain, entity);

            return entity;

        } catch (Exception e) {
            throw new PersistenceConversionException("Failed to convert domain to entity", e);
        }
    }

    @Transactional(readOnly = true)
    public TradeLoanArrangement map(TradeLoanArrangementEntity entity) {
        if (entity == null) return null;

        try {
            TradeLoanArrangement.Builder builder = TradeLoanArrangement.newBuilder(featureConfig);

            // Map identity and basic fields
            mapBasicFieldsToDomain(entity, builder);

            // Map value objects
            mapValueObjectsToDomain(entity, builder);

            // Map policies
            mapPoliciesToDomain(entity, builder);

            // Map collateral
            mapCollateralToDomain(entity, builder);

            // Map optional fields
            mapOptionalFieldsToDomain(entity, builder);

            return TradeLoanArrangement.reconstitute(builder);

        } catch (Exception e) {
            throw new PersistenceConversionException("Failed to convert entity to domain", e);
        }
    }

    private void mapBasicFieldsToEntity(TradeLoanArrangement domain, TradeLoanArrangementEntity entity) {
        entity.setId(domain.getId().value());
        entity.setCode(domain.getCode().value());
        entity.setTitle(domain.getTitle().value());
        entity.setActive(domain.getActive().isActive());
        entity.setDisabled(domain.getDisable().isDisable());

        // Map currencies
        entity.setCurrencies(
                domain.getCurrencies().stream().map(CurrencyType::getCode).collect(Collectors.toSet()));

        // Map simple fields
        entity.setGuarantorCount(domain.getGuarantorCount());
        entity.setPartyType(domain.getPartyType());
        entity.setHasInstallmentCard(domain.isHasInstallmentCard());
        entity.setAutoApproval(domain.isAutoApprovalEnabled());

        // Map enums
        entity.setLifeInsurancePaymentType(domain.getLifeInsurancePaymentType());
        entity.setLoanSecondaryType(domain.getLoanSecondaryType());
        entity.setSectionType(domain.getSectionType());
        entity.setDisbursementMethod(domain.getDisbursementMethod());
    }

    private void mapBasicFieldsToDomain(TradeLoanArrangementEntity entity, TradeLoanArrangement.Builder builder) {
        builder.withId(LoanArrangementId.of(entity.getId()))
                .withCode(LoanArrangementCode.valueOf(entity.getCode())
                        .orElseThrow(() -> new InvalidDomainStateException("Invalid code: " + entity.getCode())))
                .withTitle(Title.of(entity.getTitle())
                        .orElseThrow(() -> new InvalidDomainStateException("Invalid title: " + entity.getTitle())))
                .withActive(new Active(entity.isActive()))
                .withDisable(new Disable(entity.isDisabled()));

        // Map currencies
        Set<CurrencyType> currencies = entity.getCurrencies().stream()
                .map(code -> CurrencyType.valueOf(code)
                        .orElseThrow(() -> new InvalidDomainStateException("Invalid currency code: " + code)))
                .collect(Collectors.toSet());
        builder.withCurrencies(currencies);

        // Map simple fields
        if (entity.getGuarantorCount() != null) {
            builder.withGuarantorCount(entity.getGuarantorCount());
        }

        builder.withCustomerType(entity.getPartyType())
                .withHasInstallmentCard(entity.isHasInstallmentCard())
                .withAutoApproval(entity.isAutoApproval());

        // Map enums
        builder.withLifeInsurancePaymentType(entity.getLifeInsurancePaymentType())
                .withLoanSecondaryType(entity.getLoanSecondaryType())
                .withSectionType(entity.getSectionType());

        if (entity.getDisbursementMethod() != null) {
            builder.withDisbursementMethod(entity.getDisbursementMethod());
        }
    }

    private void mapValueObjectsToEntity(TradeLoanArrangement domain, TradeLoanArrangementEntity entity) {
        entity.setAmountRange((AmountRangeEmb) conversionService.convert(
                domain.getAmountRange(),
                TypeDescriptors.rangeOf(Money.class),
                TypeDescriptors.of(AmountRangeEmb.class)));
        entity.setDurationRange((DurationRangeEmb) conversionService.convert(
                domain.getDurationRange(),
                TypeDescriptors.rangeOf(Duration.class),
                TypeDescriptors.of(DurationRangeEmb.class)));
    }

    private void mapValueObjectsToDomain(TradeLoanArrangementEntity entity, TradeLoanArrangement.Builder builder) {
        builder.withAmountRange((Range<Money>)
                        conversionService.convert(entity.getAmountRange(), TypeDescriptors.rangeOf(Money.class)))
                .withDurationRange((Range<Duration>)
                        conversionService.convert(entity.getDurationRange(), TypeDescriptors.rangeOf(Duration.class)));
    }

    private void mapPoliciesToEntity(TradeLoanArrangement domain, TradeLoanArrangementEntity entity) {
        entity.setInterestPolicy((InterestPolicyEmb) conversionService.convert(
                domain.getInterestPolicy(),
                createPolicyDescriptor(InterestPolicy.class),
                TypeDescriptors.of(InterestPolicyEmb.class)));
        entity.setPenaltyPolicy((PenaltyPolicyEmb) conversionService.convert(
                domain.getPenaltyPolicy(),
                createPolicyDescriptor(PenaltyPolicy.class),
                TypeDescriptors.of(PenaltyPolicyEmb.class)));
        entity.setInstallmentPolicy((InstallmentPolicyEmb) conversionService.convert(
                domain.getInstallmentPolicy(),
                createPolicyDescriptor(InstallmentPolicy.class),
                TypeDescriptors.of(InstallmentPolicyEmb.class)));
        entity.setGracePeriodPolicy((GracePeriodPolicyEmb) conversionService.convert(
                domain.getGracePeriodPolicy(),
                createPolicyDescriptor(GracePeriodPolicy.class),
                TypeDescriptors.of(GracePeriodPolicyEmb.class)));
        entity.setRepaymentPriorityPolicy((RepaymentPriorityPolicyEmb) conversionService.convert(
                domain.getRepaymentPriorityPolicy(), TypeDescriptors.of(RepaymentPriorityPolicyEmb.class)));
        entity.setRegulatoryCompliancePolicy((RegulatoryCompliancePolicyEmb) conversionService.convert(
                domain.getRegulatoryCompliancePolicy(), TypeDescriptors.of(RegulatoryCompliancePolicyEmb.class)));
    }

    private void mapPoliciesToDomain(TradeLoanArrangementEntity entity, TradeLoanArrangement.Builder builder) {
        builder.withInterestPolicy((InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>)
                        conversionService.convert(
                                entity.getInterestPolicy(), createPolicyDescriptor(InterestPolicy.class)))
                .withPenaltyPolicy((PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>)
                        conversionService.convert(
                                entity.getPenaltyPolicy(), createPolicyDescriptor(PenaltyPolicy.class)))
                .withInstallmentPolicy((InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>)
                        conversionService.convert(
                                entity.getInstallmentPolicy(), createPolicyDescriptor(InstallmentPolicy.class)))
                .withGracePeriodPolicy((GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>)
                        conversionService.convert(
                                entity.getGracePeriodPolicy(), createPolicyDescriptor(GracePeriodPolicy.class)))
                .withRepaymentPriorityPolicy(
                        conversionService.convert(entity.getRepaymentPriorityPolicy(), RepaymentPriorityPolicy.class))
                .withRegulatoryCompliancePolicy(conversionService.convert(
                        entity.getRegulatoryCompliancePolicy(), RegulatoryCompliancePolicy.class));
    }

    // Collateral mapping methods
    private void mapCollateralToEntity(TradeLoanArrangement domain, TradeLoanArrangementEntity entity) {
        CollateralPolicy collateralPolicy = domain.getCollateralPolicy();
        entity.setCollateralTotalPercent(collateralPolicy.totalPercent());

        Set<CollateralTypeEntity> collateralTypes = collateralPolicy.collateralTypes().stream()
                .map(this::mapCollateralTypeToEntity)
                .collect(Collectors.toSet());

        entity.setCollateralTypes(collateralTypes);
    }

    private void mapCollateralToDomain(TradeLoanArrangementEntity entity, TradeLoanArrangement.Builder builder) {
        List<CollateralType> collateralTypes = entity.getCollateralTypes().stream()
                .map(this::mapCollateralTypeToDomain)
                .toList();

        CollateralPolicy policy = CollateralPolicy.of(
                        collateralTypes,
                        entity.getCollateralTotalPercent() != null ? entity.getCollateralTotalPercent() : 0)
                .orElseThrow(
                        () -> new InvalidDomainStateException("Cannot create CollateralPolicy from persisted data"));

        builder.withCollateralPolicy(policy);
    }

    private CollateralTypeEntity mapCollateralTypeToEntity(CollateralType type) {
        CollateralTypeEntity entity = new CollateralTypeEntity();
        entity.setName(type.name());
        entity.setDescription(type.code());
        entity.setIsRequired(false);
        return entity;
    }

    private CollateralType mapCollateralTypeToDomain(CollateralTypeEntity entity) {
        String code = entity.getDescription();
        String name = entity.getName() != null ? entity.getName() : "Unknown";

        return CollateralType.of(code, name)
                .orElseThrow(() -> new InvalidDomainStateException("Cannot create CollateralType from persisted data"));
    }

    private void mapOptionalFieldsToEntity(TradeLoanArrangement domain, TradeLoanArrangementEntity entity) {
        if (domain.getConfirmType() != null) {
            entity.setConfirmType(conversionService.convert(domain.getConfirmType(), ConfirmTypeEmb.class));
        }

        if (domain.getPreviousVersion() != null) {
            entity.setPreviousVersionId(domain.getPreviousVersion().value());
        }
    }

    private void mapOptionalFieldsToDomain(TradeLoanArrangementEntity entity, TradeLoanArrangement.Builder builder) {
        if (entity.getConfirmType() != null) {
            builder.withConfirmType(conversionService.convert(entity.getConfirmType(), ConfirmType.class));
        }

        if (entity.getPreviousVersionId() != null) {
            builder.withPreviousVersion(LoanArrangementId.of(entity.getPreviousVersionId()));
        }
    }

    private static TypeDescriptor createPolicyDescriptor(Class<?> policyClass) {
        return TypeDescriptors.parameterizedOf(
                policyClass,
                TypeDescriptors.of(TradeLoanParameterProvider.class),
                TypeDescriptors.of(TradeLoanFacilityFormulaField.class));
    }
}
