package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity;

import java.util.Objects;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.proxy.HibernateProxy;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.persistence.jpa.embeddable.MoneyEmb;
import ir.dotin.platform.pangaea.persistence.jpa.embeddable.PeriodEmb;
import ir.dotin.platform.pangaea.persistence.jpa.entity.PersistentEntity;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ConfirmTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CurrencyTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.DisbursementHistoryEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.GracePeriodEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentCountEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RevocationReasonEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.SanctionSerialEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "sanctioned_loans",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_tradesanctionedloanentity",
                    columnNames = {"sanction_serial_value"})
        })
public class TradeSanctionedLoanEntity extends PersistentEntity {

    @Nullable
    @Embedded
    private SanctionSerialEmb sanctionSerial;

    @Nullable
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "approved_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "approved_currency", length = 3))
    })
    private MoneyEmb approvedAmount;

    @Nullable
    @Embedded
    private CurrencyTypeEmb currency;

    @Nullable
    @Embedded
    private GracePeriodEmb gracePeriod;

    @Nullable
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "number", column = @Column(name = "installment_count"))})
    private InstallmentCountEmb installmentCount;

    @Nullable
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "years", column = @Column(name = "loan_duration_years")),
        @AttributeOverride(name = "months", column = @Column(name = "loan_duration_months")),
        @AttributeOverride(name = "days", column = @Column(name = "loan_duration_days"))
    })
    private PeriodEmb loanDuration;

    @Nullable
    @Column(name = "life_insurance_id")
    private String lifeInsuranceId;

    @Nullable
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "disbursement_schedule_id")
    private DisbursementScheduleEntity disbursementSchedule;

    @Nullable
    @Embedded
    private DisbursementHistoryEmb disbursementHistory;

    @Nullable
    @Embedded
    private RevocationReasonEmb revocationReason;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "disbursement_method")
    private DisbursementMethod disbursementMethod;

    @Nullable
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "value", column = @Column(name = "confirm_type", length = 50))})
    private ConfirmTypeEmb confirmType;

    @Override
    // why: getClass()-based identity is intentional for JPA entity equality (Hibernate proxy safety) — the
    // effective class is resolved via the lazy initializer so a proxy and its target compare equal.
    @SuppressWarnings("EqualsGetClass")
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy thisProxy
                ? thisProxy.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        TradeSanctionedLoanEntity that = (TradeSanctionedLoanEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy thisProxy
                ? thisProxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
