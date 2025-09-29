package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class RevocationReasonEmb {

    @Column(name = "revocation_reason", columnDefinition = "TEXT")
    private String text;
}
