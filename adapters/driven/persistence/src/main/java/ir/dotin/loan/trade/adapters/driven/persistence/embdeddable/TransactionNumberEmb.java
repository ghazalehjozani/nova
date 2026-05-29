package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;

import lombok.Data;

@Data
@Embeddable
public class TransactionNumberEmb implements Serializable {

    @Nullable
    @Column(name = "transaction_number_value", nullable = false)
    private String value;

    @Nullable
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Nullable
    @Column(name = "tracking_id", nullable = false)
    private String trackingId;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;
}
