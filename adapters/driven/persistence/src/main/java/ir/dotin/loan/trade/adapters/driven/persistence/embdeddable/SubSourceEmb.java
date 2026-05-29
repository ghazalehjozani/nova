package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class SubSourceEmb implements Serializable {

    @Nullable
    @Column(name = "sub_source_code", length = 500)
    private String code;
}
