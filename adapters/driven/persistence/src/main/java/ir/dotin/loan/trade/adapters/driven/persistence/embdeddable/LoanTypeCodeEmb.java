package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class LoanTypeCodeEmb implements Serializable {

    @Nullable
    @Column(name = "loan_type_code", nullable = false)
    private String value;
}
