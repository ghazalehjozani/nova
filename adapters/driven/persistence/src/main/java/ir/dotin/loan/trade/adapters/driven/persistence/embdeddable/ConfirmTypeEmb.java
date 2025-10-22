package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class ConfirmTypeEmb implements Serializable {

    @Column(name = "confirm_person_code")
    private String personCode;
}
