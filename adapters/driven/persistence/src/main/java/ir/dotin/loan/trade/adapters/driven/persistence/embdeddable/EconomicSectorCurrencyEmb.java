package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import lombok.Data;

@Data
@Embeddable
public class EconomicSectorCurrencyEmb implements Serializable {
    @Column(name = "economic_sector_code", nullable = false, length = 50)
    private String economicSectorCode;

    @Column(name = "economic_sector_name", nullable = false, length = 100)
    private String economicSectorName;

    @Embedded
    private CurrencyTypeEmb currencyType;
}
