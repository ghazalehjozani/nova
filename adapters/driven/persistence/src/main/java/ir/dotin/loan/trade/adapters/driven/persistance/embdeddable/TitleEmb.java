package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class TitleEmb implements Serializable {
    @Column(name = "title", nullable = false, length = 200)
    private String value;
}
