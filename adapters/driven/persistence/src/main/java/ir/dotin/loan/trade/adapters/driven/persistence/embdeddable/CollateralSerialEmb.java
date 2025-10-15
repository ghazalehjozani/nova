package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class CollateralSerialEmb implements Serializable {

    @Column(name = "collateral_serial")
    private String value;
}
