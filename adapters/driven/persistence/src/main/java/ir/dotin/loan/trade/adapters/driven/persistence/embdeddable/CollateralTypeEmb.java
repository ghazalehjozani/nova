package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class CollateralTypeEmb implements Serializable {

    @Column(name = "code", nullable = false)
    private String code;
}
