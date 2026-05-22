package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import ir.dotin.platform.pangaea.persistence.jpa.embeddable.MoneyEmb;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CollateralEmb {

    @Enumerated(EnumType.STRING)
    @Column(name = "collateral_type_code", nullable = false)
    private CollateralType collateralType;

    @Column(name = "percent", nullable = false)
    private Integer percent;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "collateral_serial", nullable = false))
    })
    private CollateralSerialEmb collateralSerial;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "used_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "used_currency", length = 3))
    })
    private MoneyEmb usedAmount;
}
