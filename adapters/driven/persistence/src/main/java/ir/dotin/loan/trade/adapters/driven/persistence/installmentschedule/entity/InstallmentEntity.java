package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity;

import java.time.LocalDate;
import java.util.Objects;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.proxy.HibernateProxy;

import ir.dotin.platform.adapter.persistence.embeddable.MoneyEmb;
import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentStatus;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentAmountEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_installments")
@Setter
@Getter
@NoArgsConstructor
public class InstallmentEntity extends PersistentEntity {

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @Embedded
    private InstallmentAmountEmb scheduledAmount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InstallmentStatus status;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "paid_amount_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "paid_amount_currency"))
    })
    private MoneyEmb paidAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "outstanding_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "outstanding_amount_currency"))
    })
    private MoneyEmb outstandingAmount;

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;

    @ManyToOne
    @JoinColumn(name = "installment_schedule_id", nullable = false)
    private InstallmentScheduleEntity installmentSchedule;

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
        InstallmentEntity that = (InstallmentEntity) o;
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
