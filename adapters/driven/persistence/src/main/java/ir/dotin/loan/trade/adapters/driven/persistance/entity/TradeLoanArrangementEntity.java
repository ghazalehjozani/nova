package ir.dotin.loan.trade.adapters.driven.persistance.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import ir.dotin.platform.adapter.persistence.embeddable.AmountRangeEmb;
import ir.dotin.platform.adapter.persistence.embeddable.DurationRangeEmb;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;
import ir.dotin.loan.trade.adapters.driven.persistance.AbstractEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.ConfirmTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.GracePeriodPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.InstallmentPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.InterestPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.PenaltyPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.RegulatoryCompliancePolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.RepaymentPriorityPolicyEmb;

@Entity
@Table(name = "trade_loan_arrangement")
public class TradeLoanArrangementEntity extends AbstractEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "disabled", nullable = false)
    private boolean disabled = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "arrangement_currencies",
            joinColumns = @JoinColumn(name = "arrangement_id"),
            indexes = @Index(name = "idx_currency_arrangement", columnList = "arrangement_id"))
    @Column(name = "currency_code", nullable = false, length = 3)
    private Set<String> currencies = new HashSet<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "minAmount", column = @Column(name = "amount_min", precision = 19, scale = 4)),
        @AttributeOverride(name = "maxAmount", column = @Column(name = "amount_max", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "amount_currency", length = 3))
    })
    private AmountRangeEmb amountRange;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "minDurationDays", column = @Column(name = "duration_min_days")),
        @AttributeOverride(name = "maxDurationDays", column = @Column(name = "duration_max_days"))
    })
    private DurationRangeEmb durationRange;

    @Column(name = "guarantor_count")
    private Integer guarantorCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false, length = 30)
    private PartyType partyType;

    @Column(name = "has_installment_card", nullable = false)
    private boolean hasInstallmentCard = false;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "personCode", column = @Column(name = "confirm_person_code", length = 50)),
        @AttributeOverride(name = "personName", column = @Column(name = "confirm_person_name", length = 100))
    })
    private ConfirmTypeEmb confirmType;

    @Enumerated(EnumType.STRING)
    @Column(name = "life_insurance_payment_type", nullable = false, length = 30)
    private LifeInsurancePaymentType lifeInsurancePaymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_secondary_type", nullable = false, length = 30)
    private LoanSecondaryType loanSecondaryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, length = 30)
    private SectionType sectionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "disbursement_method", length = 30)
    private DisbursementMethod disbursementMethod;

    @Embedded
    private InterestPolicyEmb interestPolicy;

    @Embedded
    private PenaltyPolicyEmb penaltyPolicy;

    @Embedded
    private InstallmentPolicyEmb installmentPolicy;

    @Embedded
    private GracePeriodPolicyEmb gracePeriodPolicy;

    @Embedded
    private RepaymentPriorityPolicyEmb repaymentPriorityPolicy;

    @Embedded
    private RegulatoryCompliancePolicyEmb regulatoryCompliancePolicy;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "arrangement_id")
    private Set<CollateralTypeEntity> collateralTypes = new HashSet<>();

    @Column(name = "collateral_total_percent")
    private Integer collateralTotalPercent;

    @Column(name = "auto_approval", nullable = false)
    private boolean autoApproval = false;

    @Column(name = "previous_version_id")
    private UUID previousVersionId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Set<String> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(Set<String> currencies) {
        this.currencies = currencies;
    }

    public AmountRangeEmb getAmountRange() {
        return amountRange;
    }

    public void setAmountRange(AmountRangeEmb amountRange) {
        this.amountRange = amountRange;
    }

    public DurationRangeEmb getDurationRange() {
        return durationRange;
    }

    public void setDurationRange(DurationRangeEmb durationRange) {
        this.durationRange = durationRange;
    }

    public Integer getGuarantorCount() {
        return guarantorCount;
    }

    public void setGuarantorCount(Integer guarantorCount) {
        this.guarantorCount = guarantorCount;
    }

    public PartyType getPartyType() {
        return partyType;
    }

    public void setPartyType(PartyType partyType) {
        this.partyType = partyType;
    }

    public boolean isHasInstallmentCard() {
        return hasInstallmentCard;
    }

    public void setHasInstallmentCard(boolean hasInstallmentCard) {
        this.hasInstallmentCard = hasInstallmentCard;
    }

    public ConfirmTypeEmb getConfirmType() {
        return confirmType;
    }

    public void setConfirmType(ConfirmTypeEmb confirmType) {
        this.confirmType = confirmType;
    }

    public LifeInsurancePaymentType getLifeInsurancePaymentType() {
        return lifeInsurancePaymentType;
    }

    public void setLifeInsurancePaymentType(LifeInsurancePaymentType type) {
        this.lifeInsurancePaymentType = type;
    }

    public LoanSecondaryType getLoanSecondaryType() {
        return loanSecondaryType;
    }

    public void setLoanSecondaryType(LoanSecondaryType loanSecondaryType) {
        this.loanSecondaryType = loanSecondaryType;
    }

    public SectionType getSectionType() {
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = sectionType;
    }

    public DisbursementMethod getDisbursementMethod() {
        return disbursementMethod;
    }

    public void setDisbursementMethod(DisbursementMethod disbursementMethod) {
        this.disbursementMethod = disbursementMethod;
    }

    public InterestPolicyEmb getInterestPolicy() {
        return interestPolicy;
    }

    public void setInterestPolicy(InterestPolicyEmb interestPolicy) {
        this.interestPolicy = interestPolicy;
    }

    public PenaltyPolicyEmb getPenaltyPolicy() {
        return penaltyPolicy;
    }

    public void setPenaltyPolicy(PenaltyPolicyEmb penaltyPolicy) {
        this.penaltyPolicy = penaltyPolicy;
    }

    public InstallmentPolicyEmb getInstallmentPolicy() {
        return installmentPolicy;
    }

    public void setInstallmentPolicy(InstallmentPolicyEmb installmentPolicy) {
        this.installmentPolicy = installmentPolicy;
    }

    public GracePeriodPolicyEmb getGracePeriodPolicy() {
        return gracePeriodPolicy;
    }

    public void setGracePeriodPolicy(GracePeriodPolicyEmb gracePeriodPolicy) {
        this.gracePeriodPolicy = gracePeriodPolicy;
    }

    public RepaymentPriorityPolicyEmb getRepaymentPriorityPolicy() {
        return repaymentPriorityPolicy;
    }

    public void setRepaymentPriorityPolicy(RepaymentPriorityPolicyEmb policy) {
        this.repaymentPriorityPolicy = policy;
    }

    public RegulatoryCompliancePolicyEmb getRegulatoryCompliancePolicy() {
        return regulatoryCompliancePolicy;
    }

    public void setRegulatoryCompliancePolicy(RegulatoryCompliancePolicyEmb policy) {
        this.regulatoryCompliancePolicy = policy;
    }

    public Set<CollateralTypeEntity> getCollateralTypes() {
        return collateralTypes;
    }

    public void setCollateralTypes(Set<CollateralTypeEntity> collateralTypes) {
        this.collateralTypes = collateralTypes;
    }

    public Integer getCollateralTotalPercent() {
        return collateralTotalPercent;
    }

    public void setCollateralTotalPercent(Integer collateralTotalPercent) {
        this.collateralTotalPercent = collateralTotalPercent;
    }

    public boolean isAutoApproval() {
        return autoApproval;
    }

    public void setAutoApproval(boolean autoApproval) {
        this.autoApproval = autoApproval;
    }

    public UUID getPreviousVersionId() {
        return previousVersionId;
    }

    public void setPreviousVersionId(UUID previousVersionId) {
        this.previousVersionId = previousVersionId;
    }
}
