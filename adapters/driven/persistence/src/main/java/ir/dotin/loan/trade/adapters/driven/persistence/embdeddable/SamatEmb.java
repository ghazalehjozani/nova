package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class SamatEmb implements Serializable {

    @Column(name = "tracking_number", length = 16)
    private String trackingNumber;
}
