package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class RevocationReasonEmb {

    @Nullable
    @Column(name = "revocation_reason", columnDefinition = "TEXT")
    private String text;
}
