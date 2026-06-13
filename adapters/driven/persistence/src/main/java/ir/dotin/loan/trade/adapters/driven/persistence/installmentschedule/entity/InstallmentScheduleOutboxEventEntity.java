package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import org.hibernate.proxy.HibernateProxy;

import ir.dotin.platform.pangaea.outbox.jpa.entity.AbstractOutboxEventEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "installment_schedule_outbox_events")
@NoArgsConstructor
@Setter
@Getter
public class InstallmentScheduleOutboxEventEntity extends AbstractOutboxEventEntity {

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "installment_schedule_outbox_metadata", joinColumns = @JoinColumn(name = "outbox_event_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value", length = 2000)
    private Map<String, String> metadata = new HashMap<>();

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
        InstallmentScheduleOutboxEventEntity that = (InstallmentScheduleOutboxEventEntity) o;
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
