package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity;

import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.proxy.HibernateProxy;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.persistence.jpa.entity.PersistentEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "loan_type_groups",
        indexes = @Index(name = "ix_loan_type_groups_parent", columnList = "parent_group_id"),
        uniqueConstraints = @UniqueConstraint(name = "uq_loan_type_groups_code", columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
public class LoanTypeGroupEntity extends PersistentEntity {

    @Nullable
    @Column(name = "code", nullable = false, updatable = false)
    private String code;

    @Nullable
    @Column(name = "title", nullable = false)
    private String title;

    @Nullable
    @Column(name = "parent_group_id")
    private UUID parentGroupId;

    @Override
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
        LoanTypeGroupEntity that = (LoanTypeGroupEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy thisProxy
                ? thisProxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
