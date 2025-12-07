package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class SubSourceEmb implements Serializable {

    @Column(name = "sub_source_code", length = 500)
    private String code;
}
