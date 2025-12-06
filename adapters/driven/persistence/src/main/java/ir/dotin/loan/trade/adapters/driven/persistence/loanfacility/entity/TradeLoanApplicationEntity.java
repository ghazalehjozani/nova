package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.proxy.HibernateProxy;

import ir.dotin.platform.adapter.persistence.embeddable.MoneyEmb;
import ir.dotin.platform.adapter.persistence.embeddable.PeriodEmb;
import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "loan_applications",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_tradeloanapplicationentity",
                    columnNames = {
                        "application_branch_code",
                        "loan_type_code",
                        "application_customer_number",
                        "derived_value"
                    })
        })
public class TradeLoanApplicationEntity extends PersistentEntity {

    @Column(name = "request_date", nullable = false)
    private Instant requestDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_application_parties",
            joinColumns = @JoinColumn(name = "loan_application_id"),
            indexes = @Index(name = "idx_trade_loan_application_parties", columnList = "loan_application_id"))
    private Set<PartyEmb> parties = new HashSet<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "requested_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "requested_currency", length = 3))
    })
    private MoneyEmb requestedAmount;

    @Embedded
    private CurrencyTypeEmb currency;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "years", column = @Column(name = "requested_duration_years")),
        @AttributeOverride(name = "months", column = @Column(name = "requested_duration_months")),
        @AttributeOverride(name = "days", column = @Column(name = "requested_duration_days"))
    })
    private PeriodEmb requestedLoanDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_channel", nullable = false)
    private ApplicantChannel applicantChannel;

    @Embedded
    private GracePeriodEmb gracePeriod;

    @Embedded
    private InstallmentCountEmb installmentCount;

    @Embedded
    private DisburseDestinationEmb disburseDestination;

    @Embedded
    private EconomicSectorEmb economicSector;

    @Embedded
    private BranchEmb branch;

    @Embedded
    private RequestReasonEmb requestReason;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "core", column = @Column(name = "sub_source_request_reason_code", length = 500)),
    })
    private SubSourceEmb subSource;

    @Embedded
    private DescriptionEmb description;

    @Embedded
    private CredibilityRankEmb credibilityRank;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "branch.code", column = @Column(name = "application_branch_code", nullable = false)),
        @AttributeOverride(
                name = "party.customerNumber",
                column = @Column(name = "application_customer_number", nullable = false)),
        @AttributeOverride(
                name = "party.partyType",
                column = @Column(name = "application_customer_type", nullable = false)),
        @AttributeOverride(
                name = "party.firstName",
                column = @Column(name = "application_customer_first_name", nullable = false)),
        @AttributeOverride(
                name = "party.lastName",
                column = @Column(name = "application_customer_last_name", nullable = false))
    })
    private ApplicationNumberEmb applicationNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_application_certificates",
            joinColumns = @JoinColumn(name = "loan_application_id"),
            indexes = @Index(name = "idx_trade_loan_application_certificate", columnList = "loan_application_id"))
    private Set<CertificateEmb> certificates = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "disbursement_method")
    private DisbursementMethod disbursementMethod;

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
        TradeLoanApplicationEntity that = (TradeLoanApplicationEntity) o;
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
