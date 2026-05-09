package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.proxy.HibernateProxy;

import ir.dotin.platform.adapter.persistence.embeddable.MoneyEmb;
import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CancellationDataEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CloseFacilityPaidOffInfoEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CollateralEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.TransactionNumberEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "loan_facilities")
public class TradeLoanFacilityEntity extends PersistentEntity {

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private TradeLoanApplicationEntity loanApplication;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "sanctioned_loan_id")
    private TradeSanctionedLoanEntity sanctionedLoan;

    @Column(name = "loan_type_id", nullable = false)
    private UUID loanTypeId;

    @Column(name = "loan_arrangement_id", nullable = false)
    private UUID loanArrangementId;

    @Column(name = "installment_schedule_id")
    private UUID installmentScheduleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_state", nullable = false)
    private FacilityStatus currentState;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
                name = "amount",
                column = @Column(name = "total_disbursed_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "total_disbursed_currency", length = 3))
    })
    private MoneyEmb totalDisbursedAmount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_facility_issue_contract_transaction_numbers",
            joinColumns = @JoinColumn(name = "loan_facility_id"))
    private List<TransactionNumberEmb> issueContractTransactionNumbers = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_facility_disbursement_transaction_numbers",
            joinColumns = @JoinColumn(name = "loan_facility_id"))
    private List<TransactionNumberEmb> disbursementTransactionNumbers = new ArrayList<>();

    @Embedded
    private CancellationDataEmb cancellationDataEmb;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "cancel_loan_facility_transaction_numbers",
            joinColumns = @JoinColumn(name = "loan_facility_id"))
    private List<TransactionNumberEmb> cancelLoanTransactionNumbers = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "loan_facility_accounts", joinColumns = @JoinColumn(name = "facility_id"))
    @MapKeyColumn(name = "relation_type")
    @Column(name = "account_id")
    private Map<String, String> accountInfoMap = new HashMap<>();

    @Column(name = "disbursement_date", nullable = false)
    private LocalDate disbursementDate;

    @Column(name = "facility_type")
    private String facilityType = "TRADE";

    @ElementCollection(fetch = FetchType.EAGER) // Or LAZY, depending on your needs
    @CollectionTable(
            name = "loan_facility_collaterals",
            joinColumns = @JoinColumn(name = "loan_facility_id"),
            indexes = @Index(name = "idx_trade_loan_facility_collaterals", columnList = "loan_facility_id"))
    private List<CollateralEmb> collaterals = new ArrayList<>();

    @Embedded
    private CloseFacilityPaidOffInfoEmb closeFacilityPaidOffInfo;

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
        TradeLoanFacilityEntity that = (TradeLoanFacilityEntity) o;
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
