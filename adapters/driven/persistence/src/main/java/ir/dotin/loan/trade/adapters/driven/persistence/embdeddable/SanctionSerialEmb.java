package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;

import lombok.Data;

@Data
@Embeddable
public class SanctionSerialEmb implements Serializable {

    @Nullable
    @Column(name = "sanction_serial_value", nullable = false)
    private String value;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "sanction_type", nullable = false)
    private SanctionType type;
}
