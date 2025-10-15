package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class PartyEmb implements Serializable {

    @Column(name = "customer_number", nullable = false)
    private String customerNumber;

    @Column(name = "party_type", nullable = false)
    private String partyType;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;
}
