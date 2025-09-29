package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class CollateralSerialEmb implements Serializable {

    @Column(name = "collateral_serial", length = 100)
    private String value;
}
