package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class EconomicSectorCurrencyEmb implements Serializable {

    @Nullable
    @Column(name = "economic_sector_code", nullable = false)
    private String economicSectorCode;

    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "economic_sector_currency_types", columnDefinition = "jsonb")
    private Set<String> currencyTypes;
}
