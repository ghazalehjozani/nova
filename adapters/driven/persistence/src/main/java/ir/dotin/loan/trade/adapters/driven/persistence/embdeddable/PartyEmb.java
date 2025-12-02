package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;

import lombok.Data;

@Data
@Embeddable
public class PartyEmb implements Serializable {

    @Column(name = "customer_number", nullable = false)
    private String customerNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false)
    private PartyType partyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_role", nullable = false)
    private PartyRole partyRole;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;
}
