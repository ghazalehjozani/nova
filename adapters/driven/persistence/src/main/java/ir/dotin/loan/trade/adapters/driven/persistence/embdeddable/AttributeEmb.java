package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.commons.domain.vo.ValueType;

import lombok.Data;

@Data
@Embeddable
public class AttributeEmb implements Serializable {

    @Nullable
    @Column(name = "attribute_name", nullable = false)
    private String name;

    @Nullable
    @Column(name = "attribute_code", nullable = false)
    private String code;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private ValueType dataType;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory;
}
