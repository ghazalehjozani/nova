package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class RespiteSerialEmb implements Serializable {

    @Nullable
    @Column(name = "respite_serial_value")
    private String value;
}
