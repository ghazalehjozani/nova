package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.time.LocalDate;
import java.util.Objects;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import ir.dotin.platform.pangaea.persistence.jpa.embeddable.MoneyEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Setter
@Getter
@NoArgsConstructor
public class InstallmentPaymentEmb {

    @Column(name = "installment_sequence_number", nullable = false)
    private Integer installmentSequenceNumber;

    @Column(name = "payment_reference", nullable = false)
    private String paymentReference;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "principal_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "principal_amount_currency"))
    })
    private MoneyEmb principalAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "interest_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "interest_amount_currency"))
    })
    private MoneyEmb interestAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_paid_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "total_paid_amount_currency"))
    })
    private MoneyEmb totalPaidAmount;

    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "channel")
    private String channel;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(name = "remarks")
    private String remarks;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstallmentPaymentEmb that = (InstallmentPaymentEmb) o;
        return Objects.equals(paymentReference, that.paymentReference)
                && Objects.equals(totalPaidAmount, that.totalPaidAmount)
                && Objects.equals(paymentDate, that.paymentDate)
                && Objects.equals(installmentSequenceNumber, that.installmentSequenceNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentReference, totalPaidAmount, paymentDate, installmentSequenceNumber);
    }
}
