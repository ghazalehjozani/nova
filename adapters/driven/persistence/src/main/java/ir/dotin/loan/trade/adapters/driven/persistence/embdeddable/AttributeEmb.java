package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import ir.dotin.platform.pangaea.commons.domain.vo.ValueType;

import lombok.Data;

@Data
@Embeddable
public class AttributeEmb implements Serializable {

    @Column(name = "attribute_name", nullable = false)
    private String name;

    @Column(name = "attribute_code", nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private ValueType dataType;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory;
}
