package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.persistence.jpa.embeddable.MoneyEmb;

import lombok.Data;

@Data
@Embeddable
public class ScheduledTrancheEmb implements Serializable {

    @Nullable
    @Column(name = "scheduled_date")
    private Instant scheduledDate;

    @Nullable
    @Embedded
    private MoneyEmb amount;
}
