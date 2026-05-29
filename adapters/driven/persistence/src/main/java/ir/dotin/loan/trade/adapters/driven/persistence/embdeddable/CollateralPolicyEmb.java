package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.CollateralCalculationType;

import lombok.Data;

@Data
@Embeddable
public class CollateralPolicyEmb implements Serializable {

    @Nullable
    @Column(name = "collateral_total_percent")
    private Integer totalPercent;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_arrangement_collateral_types",
            joinColumns = @JoinColumn(name = "loan_arrangement_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "collateral_code")
    private Set<CollateralType> collateralTypes = new HashSet<>();

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "collateral_calculation_type", nullable = false)
    private CollateralCalculationType collateralCalculationType;
}
