package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import ir.dotin.platform.adapter.persistence.embeddable.MoneyEmb;

import lombok.Data;

@Data
@Embeddable
public class InstallmentAmountEmb implements Serializable {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
                name = "amount",
                column = @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "total_amount_currency", nullable = false))
    })
    private MoneyEmb totalAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
                name = "amount",
                column = @Column(name = "principal_amount", precision = 19, scale = 4, nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "principal_amount_currency", nullable = false))
    })
    private MoneyEmb principalAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
                name = "amount",
                column = @Column(name = "interest_amount", precision = 19, scale = 4, nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "interest_amount_currency", nullable = false))
    })
    private MoneyEmb interestAmount;
}
