package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class SubSourceEmb implements Serializable {

    @Column(name = "request_reason_code", length = 500)
    private String core;

    @Column(name = "request_reason_name", length = 500)
    private String name;
}
