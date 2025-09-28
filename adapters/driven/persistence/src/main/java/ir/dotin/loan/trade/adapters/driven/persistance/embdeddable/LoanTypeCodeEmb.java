package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class LoanTypeCodeEmb implements Serializable {
    @Column(name = "loan_type_code", nullable = false, length = 50)
    private String value;
}
