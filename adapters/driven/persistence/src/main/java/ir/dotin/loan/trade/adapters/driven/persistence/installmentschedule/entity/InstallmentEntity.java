package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.proxy.HibernateProxy;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.persistence.jpa.embeddable.MoneyEmb;
import ir.dotin.platform.pangaea.persistence.jpa.entity.PersistentEntity;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentStatus;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentAmountEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentPaymentEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_installments")
@Setter
@Getter
@NoArgsConstructor
public class InstallmentEntity extends PersistentEntity {

    @Nullable
    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @Nullable
    @Embedded
    private InstallmentAmountEmb scheduledAmount;

    @Nullable
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InstallmentStatus status;

    @Nullable
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "paid_amount_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "paid_amount_currency"))
    })
    private MoneyEmb paidAmount;

    @Nullable
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "outstanding_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "outstanding_amount_currency"))
    })
    private MoneyEmb outstandingAmount;

    @Nullable
    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "installment_schedule_id", nullable = false)
    private InstallmentScheduleEntity installmentSchedule;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "loan_installment_payments", joinColumns = @JoinColumn(name = "installment_id"))
    private List<InstallmentPaymentEmb> payments = new ArrayList<>();

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
