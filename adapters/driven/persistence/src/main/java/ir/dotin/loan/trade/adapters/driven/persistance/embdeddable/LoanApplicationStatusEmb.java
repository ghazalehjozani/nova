package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class LoanApplicationStatusEmb implements Serializable {
    @Column(name = "is_allowed", nullable = false)
    private boolean isAllowed;
}
