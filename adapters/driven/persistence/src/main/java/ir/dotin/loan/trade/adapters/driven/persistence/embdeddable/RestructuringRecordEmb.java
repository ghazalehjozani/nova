package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestructuringRecordEmb implements Serializable {

    @Column(name = "restructuring_reason", length = 500)
    private String reason;

    @Column(name = "restructuring_amount", precision = 19, scale = 4)
    private BigDecimal restructuringAmount;

    @Column(name = "restructuring_amount_currency", length = 3)
    private String restructuringAmountCurrency;

    @Column(name = "previous_installment_count")
    private Integer previousInstallmentCount;

    @Column(name = "new_installment_count")
    private Integer newInstallmentCount;

    @Column(name = "unpaid_installments_count")
    private Integer unpaidInstallmentsCount;

    @Column(name = "preserved_installments_count")
    private Integer preservedInstallmentsCount;

    @Column(name = "restructured_at")
    private Instant restructuredAt;

    @Column(name = "restructured_by", length = 50)
    private String restructuredBy;
}
