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

import org.hibernate.proxy.HibernateProxy;

import ir.dotin.platform.adapter.persistence.embeddable.MoneyEmb;
import ir.dotin.platform.adapter.persistence.embeddable.PeriodEmb;
import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CollateralSerialEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CurrencyTypeEmb;
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
@Table(name = "sanctioned_loans")
public class TradeSanctionedLoanEntity extends PersistentEntity {

    @Embedded
    private SanctionSerialEmb sanctionSerial;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "approved_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "approved_currency", length = 3))
    })
    private MoneyEmb approvedAmount;

    @Embedded
    private CurrencyTypeEmb currency;

    @Embedded
    private GracePeriodEmb gracePeriod;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "number", column = @Column(name = "installment_count"))})
    private InstallmentCountEmb installmentCount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "years", column = @Column(name = "loan_duration_years")),
        @AttributeOverride(name = "months", column = @Column(name = "loan_duration_months")),
        @AttributeOverride(name = "days", column = @Column(name = "loan_duration_days"))
    })
    private PeriodEmb loanDuration;

    @Column(name = "life_insurance_id")
    private String lifeInsuranceId;

    @Embedded
    private CollateralSerialEmb collateralSerial;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "disbursement_schedule_id", nullable = false)
    private DisbursementScheduleEntity disbursementSchedule;

    @Embedded
    private RevocationReasonEmb revocationReason;

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
        TradeSanctionedLoanEntity that = (TradeSanctionedLoanEntity) o;
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
