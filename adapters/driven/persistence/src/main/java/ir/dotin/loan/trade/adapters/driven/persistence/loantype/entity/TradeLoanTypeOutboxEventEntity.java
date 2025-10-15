package ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity;

import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import ir.dotin.platform.adapter.messaging.persistence.entity.AbstractOutboxEventEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_type_outbox_events")
@NoArgsConstructor
@Getter
@Setter
public class TradeLoanTypeOutboxEventEntity extends AbstractOutboxEventEntity {

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "loan_type_outbox_metadata", joinColumns = @JoinColumn(name = "outbox_event_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value", length = 2000)
    private Map<String, String> metadata = new HashMap<>();
}
