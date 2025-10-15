package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class CredibilityRankEmb implements Serializable {

    @Column(name = "credibility_rank")
    private String value;
}
