package ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.entity;

import java.time.LocalDate;
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

import ir.dotin.platform.adapter.persistence.embeddable.MoneyEmb;
import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentStatus;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.InstallmentAmountEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_installment")
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
        @AttributeOverride(
                name = "amount",
                column = @Column(name = "paid_amount_amount", precision = 19, scale = 4, nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "paid_amount_currency", nullable = false))
    })
    private MoneyEmb paidAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
                name = "amount",
                column = @Column(name = "outstanding_amount", precision = 19, scale = 4, nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "outstanding_amount_currency", nullable = false))
    })
    private MoneyEmb outstandingAmount;

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;

    @ManyToOne
    @JoinColumn(name = "installment_schedule_id", nullable = false)
    private InstallmentScheduleEntity installmentSchedule;
}
