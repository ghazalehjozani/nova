package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import ir.dotin.platform.adapter.persistence.embeddable.MoneyEmb;

import lombok.Data;

@Data
@Embeddable
public class ScheduledTrancheEmb implements Serializable {

    @Column(name = "scheduled_date")
    private Instant scheduledDate;

    @Embedded
    private MoneyEmb amount;
}
