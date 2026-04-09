package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;

import lombok.Data;

@Data
@Embeddable
public class TransactionNumberEmb implements Serializable {

    @Column(name = "transaction_number_value", nullable = false)
    private String value;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "tracking_id", nullable = false)
    private String trackingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;
}
