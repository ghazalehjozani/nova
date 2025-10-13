package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class InstallmentCountEmb implements Serializable {

    @Column(name = "installment_count", nullable = false)
    private Integer value;
}
