package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class ConfirmTypeEmb implements Serializable {

    @Column(name = "confirm_person_code", length = 50)
    private String personCode;

    @Column(name = "confirm_person_name", length = 100)
    private String personName;
}
