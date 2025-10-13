package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class PartyEmb implements Serializable {

    @Column(name = "customer_number", nullable = false, length = 100)
    private String customerNumber;

    @Column(name = "party_type", nullable = false, length = 30)
    private String partyType;

    @Column(name = "first_name", nullable = false, length = 200)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 200)
    private String lastName;
}
