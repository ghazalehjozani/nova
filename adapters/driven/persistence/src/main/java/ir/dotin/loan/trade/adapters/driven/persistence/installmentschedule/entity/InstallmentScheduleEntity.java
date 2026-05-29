package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import org.hibernate.proxy.HibernateProxy;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.persistence.jpa.embeddable.MoneyEmb;
import ir.dotin.platform.pangaea.persistence.jpa.entity.PersistentEntity;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleType;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CurrencyTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.GracePeriodEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RestructuringRecordEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ScheduleHistoryEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_installment_schedules")
@Setter
@Getter
@NoArgsConstructor
public class InstallmentScheduleEntity extends PersistentEntity {

    @OneToMany(
            mappedBy = "installmentSchedule",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    private List<InstallmentEntity> installments = new ArrayList<>();

    @Nullable
    @Embedded
    private ScheduleHistoryEmb scheduleHistory;

    @Nullable
    @Column(name = "loan_facility_id", nullable = false)
    private UUID loanFacilityId;

    @Nullable
    @Embedded
    private MoneyEmb totalLoanAmount;

    @Nullable
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "schedule_currency", nullable = false, length = 3))
    })
    private CurrencyTypeEmb currency;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private InstallmentScheduleType scheduleType;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InstallmentScheduleStatus status;

    @Nullable
    @Column(name = "initiated_at", nullable = false)
    private Instant initiatedAt;

    @Nullable
    @Column(name = "last_modified_at", nullable = false)
    private Instant lastModifiedAt;

    @Nullable
    @Embedded
    private GracePeriodEmb gracePeriod;

    @Nullable
    @Column(name = "interest_rate", precision = 10, scale = 6, nullable = false)
    private BigDecimal interestRate;

    @Nullable
    @Embedded
    private RestructuringRecordEmb restructuringRecord;

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
        InstallmentScheduleEntity that = (InstallmentScheduleEntity) o;
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
