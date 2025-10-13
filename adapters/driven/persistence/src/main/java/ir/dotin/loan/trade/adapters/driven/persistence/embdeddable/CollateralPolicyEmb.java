package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;

import lombok.Data;

@Data
@Embeddable
public class CollateralPolicyEmb implements Serializable {

    @Column(name = "collateral_total_percent")
    private Integer totalPercent;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_arrangement_collateral_types",
            joinColumns = @JoinColumn(name = "loan_arrangement_id"),
            indexes = @Index(name = "idx_trade_loan_arrangement_collateral", columnList = "loan_arrangement_id"))
    @AttributeOverrides({
        @AttributeOverride(name = "code", column = @Column(name = "collateral_code", length = 50)),
        @AttributeOverride(name = "name", column = @Column(name = "collateral_name", length = 100))
    })
    private Set<CollateralTypeEmb> collateralTypes = new HashSet<>();
}
