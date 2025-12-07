package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.time.LocalDate;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import ir.dotin.platform.adapter.persistence.embeddable.MoneyEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class DisbursementRecordEmb {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "disbursed_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "disbursed_currency", length = 3))
    })
    private MoneyEmb amount;

    @Column(name = "disbursed_at", nullable = false)
    private LocalDate disbursedAt;

    @Column(name = "disbursed_by", length = 50)
    private String disbursedBy;
}
