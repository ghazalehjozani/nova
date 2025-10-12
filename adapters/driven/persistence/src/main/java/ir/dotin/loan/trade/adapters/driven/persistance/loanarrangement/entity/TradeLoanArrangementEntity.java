package ir.dotin.loan.trade.adapters.driven.persistance.loanarrangement.entity;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import ir.dotin.platform.adapter.persistence.embeddable.AmountRangeEmb;
import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.CollateralPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.ConfirmTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.CurrencyTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.EconomicSectorEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.GracePeriodPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.InstallmentPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.InterestPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.PenaltyPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.PeriodRangeEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.RegulatoryCompliancePolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.RepaymentPriorityPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.TitleEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_arrangements")
@Setter
@Getter
@NoArgsConstructor
public class TradeLoanArrangementEntity extends PersistentEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Embedded
    private TitleEmb title;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "disable", nullable = false)
    private boolean disable = false;

    @Embedded
    private CurrencyTypeEmb currencyType;

    @Embedded
    private EconomicSectorEmb economicSector;

    @Embedded
    private AmountRangeEmb amountRange;

    @Embedded
    private PeriodRangeEmb durationRange;

    @Column(name = "guarantor_count")
    private Integer guarantorCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false, length = 30)
    private PartyType partyType;

    @Column(name = "has_installment_card", nullable = false)
    private boolean hasInstallmentCard = false;

    @Embedded
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

    @Embedded
    private CollateralPolicyEmb collateralPolicy;

    @Column(name = "auto_approval", nullable = false)
    private boolean autoApproval = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "disbursement_method", length = 30)
    private DisbursementMethod disbursementMethod;

    @Column(name = "previous_version_id")
    private UUID previousVersion;
}
