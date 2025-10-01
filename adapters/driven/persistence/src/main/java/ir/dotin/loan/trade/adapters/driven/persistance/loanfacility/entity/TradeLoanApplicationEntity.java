package ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.entity;

import java.time.Instant;
import java.util.HashSet;
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

import ir.dotin.platform.adapter.persistence.embeddable.MoneyEmb;
import ir.dotin.platform.adapter.persistence.embeddable.PeriodEmb;
import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.ApplicationNumberEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.BranchEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.CertificateEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.CredibilityRankEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.DescriptionEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.DisburseDestinationEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.EconomicSectorEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.GracePeriodEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.InstallmentCountEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.PartyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.RequestReasonEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.SubSourceEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "loan_application")
public class TradeLoanApplicationEntity extends PersistentEntity {

    @Column(name = "request_date", nullable = false)
    private Instant requestDate;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
                name = "customerNumber",
                column = @Column(name = "customer_number", nullable = false, length = 100)),
        @AttributeOverride(name = "partyType", column = @Column(name = "customer_type", nullable = false, length = 20)),
        @AttributeOverride(
                name = "firstName",
                column = @Column(name = "customer_first_name", nullable = false, length = 100)),
        @AttributeOverride(
                name = "lastName",
                column = @Column(name = "customer_last_name", nullable = false, length = 100))
    })
    private PartyEmb customer;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "requested_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "requested_currency", length = 3))
    })
    private MoneyEmb requestedAmount;

    @Embedded
    private CurrencyType currency;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "years", column = @Column(name = "requested_duration_years")),
        @AttributeOverride(name = "months", column = @Column(name = "requested_duration_months")),
        @AttributeOverride(name = "days", column = @Column(name = "requested_duration_days"))
    })
    private PeriodEmb requestedLoanDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_channel", nullable = false, length = 30)
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
        @AttributeOverride(name = "name", column = @Column(name = "sub_source_request_reason_name", length = 500))
    })
    private SubSourceEmb subSource;

    @Embedded
    private DescriptionEmb description;

    @Embedded
    private CredibilityRankEmb credibilityRank;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
                name = "branch.code",
                column = @Column(name = "application_branch_code", nullable = false, length = 50)),
        @AttributeOverride(
                name = "branch.name",
                column = @Column(name = "application_branch_name", nullable = false, length = 200)),
        @AttributeOverride(
                name = "party.customerNumber",
                column = @Column(name = "application_customer_number", nullable = false, length = 100)),
        @AttributeOverride(
                name = "party.partyType",
                column = @Column(name = "application_customer_type", nullable = false, length = 20)),
        @AttributeOverride(
                name = "party.firstName",
                column = @Column(name = "application_customer_first_name", nullable = false, length = 100)),
        @AttributeOverride(
                name = "party.lastName",
                column = @Column(name = "application_customer_last_name", nullable = false, length = 100))
    })
    private ApplicationNumberEmb applicationNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_application_guarantors",
            joinColumns = @JoinColumn(name = "loan_application_id"),
            indexes = @Index(name = "idx_trade_loan_application_guarantor", columnList = "loan_application_id"))
    private Set<PartyEmb> guarantors = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_application_certificates",
            joinColumns = @JoinColumn(name = "loan_application_id"),
            indexes = @Index(name = "idx_trade_loan_application_certificate", columnList = "loan_application_id"))
    private Set<CertificateEmb> certificates = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "disbursement_method")
    private DisbursementMethod disbursementMethod;
}
