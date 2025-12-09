package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import lombok.Data;

@Data
@Embeddable
public class ApplicationNumberEmb implements Serializable {

    @Embedded
    private BranchEmb branch;

    @Embedded
    private LoanTypeCodeEmb loanTypeCode;

    @Embedded
    private ApplicationPartyEmb party;

    @Column(name = "derived_value", nullable = false)
    private String derivedValue;
}
