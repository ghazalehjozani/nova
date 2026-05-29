package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import org.hibernate.proxy.HibernateProxy;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.persistence.jpa.embeddable.AmountRangeEmb;
import ir.dotin.platform.pangaea.persistence.jpa.entity.PersistentEntity;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CollateralPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ConfirmTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CurrencyTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.EconomicSectorEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.GracePeriodPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InterestPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.PenaltyPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.PeriodRangeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RegulatoryCompliancePolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RepaymentPriorityPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.TitleEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_arrangements")
@Setter
@Getter
@NoArgsConstructor
public class TradeLoanArrangementEntity extends PersistentEntity {

    @Nullable
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Nullable
    @Embedded
    private TitleEmb title;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "disable", nullable = false)
    private boolean disable = false;

    @Nullable
    @Embedded
    private CurrencyTypeEmb currencyType;

    @Nullable
    @Embedded
    private EconomicSectorEmb economicSector;

    @Nullable
    @Embedded
    private AmountRangeEmb amountRange;

    @Nullable
    @Embedded
    private PeriodRangeEmb durationRange;

    @Nullable
    @Column(name = "guarantor_count")
    private Integer guarantorCount;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false)
    private PartyType partyType;

    @Column(name = "has_installment_card", nullable = false)
    private boolean hasInstallmentCard = false;

    @Nullable
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "loan_arrangement_confirm_types", joinColumns = @JoinColumn(name = "loan_arrangement_id"))
    private List<ConfirmTypeEmb> confirmTypes;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "life_insurance_payment_type", nullable = false)
    private LifeInsurancePaymentType lifeInsurancePaymentType;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "loan_secondary_type", nullable = false)
    private LoanSecondaryType loanSecondaryType;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false)
    private SectionType sectionType;

    @Nullable
    @Embedded
    private InterestPolicyEmb interestPolicy;

    @Nullable
    @Embedded
    private PenaltyPolicyEmb penaltyPolicy;

    @Nullable
    @Embedded
    private InstallmentPolicyEmb installmentPolicy;

    @Nullable
    @Embedded
    private GracePeriodPolicyEmb gracePeriodPolicy;

    @Nullable
    @Embedded
    private RepaymentPriorityPolicyEmb repaymentPriorityPolicy;

    @Nullable
    @Embedded
    private RegulatoryCompliancePolicyEmb regulatoryCompliancePolicy;

    @Nullable
    @Embedded
    private CollateralPolicyEmb collateralPolicy;

    @Nullable
    @Column(name = "previous_version_id")
    private UUID previousVersion;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "disbursement_type")
    private DisbursementType disbursementType;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        TradeLoanArrangementEntity that = (TradeLoanArrangementEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this)
                        .getHibernateLazyInitializer()
                        .getPersistentClass()
                        .hashCode()
                : getClass().hashCode();
    }
}
