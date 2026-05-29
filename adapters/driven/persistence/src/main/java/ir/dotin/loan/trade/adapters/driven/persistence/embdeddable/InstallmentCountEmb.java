package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class InstallmentCountEmb implements Serializable {

    @Nullable
    @Column(name = "installment_count", nullable = false)
    private Integer value;
}
