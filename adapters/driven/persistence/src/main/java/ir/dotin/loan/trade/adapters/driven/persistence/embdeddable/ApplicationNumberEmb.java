package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class ApplicationNumberEmb implements Serializable {

    @Nullable
    @Embedded
    private BranchEmb branch;

    @Nullable
    @Embedded
    private LoanTypeCodeEmb loanTypeCode;

    @Nullable
    @Embedded
    private ApplicationPartyEmb party;

    @Nullable
    @Column(name = "derived_value", nullable = false)
    private String derivedValue;
}
