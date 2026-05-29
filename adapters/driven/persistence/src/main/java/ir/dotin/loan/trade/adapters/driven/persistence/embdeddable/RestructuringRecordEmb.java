package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

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

    @Nullable
    @Column(name = "restructuring_reason", length = 500)
    private String reason;

    @Nullable
    @Column(name = "restructuring_amount", precision = 19, scale = 4)
    private BigDecimal restructuringAmount;

    @Nullable
    @Column(name = "restructuring_amount_currency", length = 3)
    private String restructuringAmountCurrency;

    @Nullable
    @Column(name = "previous_installment_count")
    private Integer previousInstallmentCount;

    @Nullable
    @Column(name = "new_installment_count")
    private Integer newInstallmentCount;

    @Nullable
    @Column(name = "unpaid_installments_count")
    private Integer unpaidInstallmentsCount;

    @Nullable
    @Column(name = "preserved_installments_count")
    private Integer preservedInstallmentsCount;

    @Nullable
    @Column(name = "restructured_at")
    private Instant restructuredAt;

    @Nullable
    @Column(name = "restructured_by", length = 50)
    private String restructuredBy;
}
