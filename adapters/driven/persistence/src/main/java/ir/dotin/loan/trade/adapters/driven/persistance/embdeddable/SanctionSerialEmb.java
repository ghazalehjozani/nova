package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;

import lombok.Data;

@Data
@Embeddable
public class SanctionSerialEmb implements Serializable {

    @Column(name = "sanction_serial_value", nullable = false, length = 100)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "sanction_type", nullable = false, length = 20)
    private SanctionType type;
}
