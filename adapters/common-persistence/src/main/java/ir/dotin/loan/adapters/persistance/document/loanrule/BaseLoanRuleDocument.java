package ir.dotin.loan.adapters.persistance.document.loanrule;

import ir.dotin.loan.adapters.persistance.document.base.CurrencyDocument;
import ir.dotin.loan.adapters.persistance.document.base.DurationRangeDocument;
import ir.dotin.loan.adapters.persistance.document.base.EconomicSectorsDocument;
import ir.dotin.loan.adapters.persistance.document.base.MoneyRangeDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.CollateralPolicyDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.ConfirmTypeDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.GracePeriodPolicyDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.InstallmentPolicyDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.InterestPolicyDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.PenaltyPolicyDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.RegulatoryCompliancePolicyDocument;
import ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments.RepaymentPriorityPolicyDocument;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("loan_rules")
public class BaseLoanRuleDocument {

    @Id
    private UUID id;

    // General Information
    private String code;
    private String title;

    // Configuration
    private boolean active;
    private boolean disable;

    // Applicable Sectors and Currencies
    private Set<EconomicSectorsDocument> economicSectors;
    private Set<CurrencyDocument> currencies;

    // Constraints
    private MoneyRangeDocument amountRange;
    private DurationRangeDocument durationRange;
    private Integer guarantorCount;
    private String customerType;
    private boolean hasInstallmentCard;

    // Types
    private ConfirmTypeDocument confirmType;
    private String disburseType;
    private String lifeInsurancePaymentType;
    private String ruleDisburseType;
    private String loanSecondaryType;
    private String sectionType;

    // Policies
    private InterestPolicyDocument interestPolicy;
    private PenaltyPolicyDocument penaltyPolicy;
    private InstallmentPolicyDocument installmentPolicy;
    private GracePeriodPolicyDocument gracePeriodPolicy;
    private RepaymentPriorityPolicyDocument repaymentPriorityPolicy;
    private RegulatoryCompliancePolicyDocument regulatoryCompliancePolicy;
    private CollateralPolicyDocument collateralPolicy;

    // Relationship
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

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public Set<EconomicSectorsDocument> getEconomicSectors() {
        return economicSectors;
    }

    public void setEconomicSectors(
            Set<EconomicSectorsDocument> economicSectors) {
        this.economicSectors = economicSectors;
    }

    public Set<CurrencyDocument> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(Set<CurrencyDocument> currencies) {
        this.currencies = currencies;
    }

    public MoneyRangeDocument getAmountRange() {
        return amountRange;
    }

    public void setAmountRange(
            MoneyRangeDocument amountRange) {
        this.amountRange = amountRange;
    }

    public DurationRangeDocument getDurationRange() {
        return durationRange;
    }

    public void setDurationRange(
            DurationRangeDocument durationRange) {
        this.durationRange = durationRange;
    }

    public Integer getGuarantorCount() {
        return guarantorCount;
    }

    public void setGuarantorCount(Integer guarantorCount) {
        this.guarantorCount = guarantorCount;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public boolean isHasInstallmentCard() {
        return hasInstallmentCard;
    }

    public void setHasInstallmentCard(boolean hasInstallmentCard) {
        this.hasInstallmentCard = hasInstallmentCard;
    }

    public ConfirmTypeDocument getConfirmType() {
        return confirmType;
    }

    public void setConfirmType(ConfirmTypeDocument confirmType) {
        this.confirmType = confirmType;
    }

    public String getDisburseType() {
        return disburseType;
    }

    public void setDisburseType(String disburseType) {
        this.disburseType = disburseType;
    }

    public String getLifeInsurancePaymentType() {
        return lifeInsurancePaymentType;
    }

    public void setLifeInsurancePaymentType(String lifeInsurancePaymentType) {
        this.lifeInsurancePaymentType = lifeInsurancePaymentType;
    }

    public String getRuleDisburseType() {
        return ruleDisburseType;
    }

    public void setRuleDisburseType(String ruleDisburseType) {
        this.ruleDisburseType = ruleDisburseType;
    }

    public String getLoanSecondaryType() {
        return loanSecondaryType;
    }

    public void setLoanSecondaryType(String loanSecondaryType) {
        this.loanSecondaryType = loanSecondaryType;
    }

    public String getSectionType() {
        return sectionType;
    }

    public void setSectionType(String sectionType) {
        this.sectionType = sectionType;
    }

    public InterestPolicyDocument getInterestPolicy() {
        return interestPolicy;
    }

    public void setInterestPolicy(
            InterestPolicyDocument interestPolicy) {
        this.interestPolicy = interestPolicy;
    }

    public PenaltyPolicyDocument getPenaltyPolicy() {
        return penaltyPolicy;
    }

    public void setPenaltyPolicy(
            PenaltyPolicyDocument penaltyPolicy) {
        this.penaltyPolicy = penaltyPolicy;
    }

    public InstallmentPolicyDocument getInstallmentPolicy() {
        return installmentPolicy;
    }

    public void setInstallmentPolicy(
            InstallmentPolicyDocument installmentPolicy) {
        this.installmentPolicy = installmentPolicy;
    }

    public GracePeriodPolicyDocument getGracePeriodPolicy() {
        return gracePeriodPolicy;
    }

    public void setGracePeriodPolicy(
            GracePeriodPolicyDocument gracePeriodPolicy) {
        this.gracePeriodPolicy = gracePeriodPolicy;
    }

    public RepaymentPriorityPolicyDocument getRepaymentPriorityPolicy() {
        return repaymentPriorityPolicy;
    }

    public void setRepaymentPriorityPolicy(
            RepaymentPriorityPolicyDocument repaymentPriorityPolicy) {
        this.repaymentPriorityPolicy = repaymentPriorityPolicy;
    }

    public RegulatoryCompliancePolicyDocument getRegulatoryCompliancePolicy() {
        return regulatoryCompliancePolicy;
    }

    public void setRegulatoryCompliancePolicy(
            RegulatoryCompliancePolicyDocument regulatoryCompliancePolicy) {
        this.regulatoryCompliancePolicy = regulatoryCompliancePolicy;
    }

    public CollateralPolicyDocument getCollateralPolicy() {
        return collateralPolicy;
    }

    public void setCollateralPolicy(
            CollateralPolicyDocument collateralPolicy) {
        this.collateralPolicy = collateralPolicy;
    }

    public UUID getPreviousVersionId() {
        return previousVersionId;
    }

    public void setPreviousVersionId(UUID previousVersionId) {
        this.previousVersionId = previousVersionId;
    }
}

