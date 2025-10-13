package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity;

import java.util.List;
import java.util.Objects;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import org.hibernate.proxy.HibernateProxy;

import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ScheduledTrancheEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_disbursement_schedules")
@Setter
@Getter
@NoArgsConstructor
public class DisbursementScheduleEntity extends PersistentEntity {

    @ElementCollection
    @CollectionTable(
            name = "loan_disbursement_schedule_tranches",
            joinColumns = @JoinColumn(name = "disbursement_schedule_id"))
    private List<ScheduledTrancheEmb> tranches;

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
        DisbursementScheduleEntity that = (DisbursementScheduleEntity) o;
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
