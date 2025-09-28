// TrackedTransactionNumberEmb.java
package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import lombok.Data;

@Data
@Embeddable
public class TransactionNumberEmb implements Serializable {

    @Column(name = "transaction_number_value", nullable = false, length = 50)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 50)
    private TradeRelationType relationType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "tracking_id", nullable = false, length = 100)
    private String trackingId;
}
