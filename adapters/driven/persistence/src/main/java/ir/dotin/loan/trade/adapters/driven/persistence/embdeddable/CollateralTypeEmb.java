package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;

import lombok.Data;

@Data
@Embeddable
public class CollateralTypeEmb implements Serializable {

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "collateral_code")
    private CollateralType type;
}
