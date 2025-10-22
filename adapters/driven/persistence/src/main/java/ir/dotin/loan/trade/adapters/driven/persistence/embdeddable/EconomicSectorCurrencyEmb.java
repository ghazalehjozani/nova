package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Data;

@Data
@Embeddable
public class EconomicSectorCurrencyEmb implements Serializable {
    @Column(name = "economic_sector_code", nullable = false)
    private String economicSectorCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "economic_sector_currency_types", columnDefinition = "jsonb")
    private Set<String> currencyTypes;
}
