package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;

import lombok.Data;

@Data
@Embeddable
public class PartyEmb implements Serializable {

    @Nullable
    @Column(name = "customer_number", nullable = false)
    private String customerNumber;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false)
    private PartyType partyType;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "party_role", nullable = false)
    private PartyRole partyRole;

    @Nullable
    @Column(name = "first_name")
    private String firstName;

    @Nullable
    @Column(name = "last_name")
    private String lastName;

    @Nullable
    @Column(name = "company_name")
    private String companyName;

    @Nullable
    @Column(name = "guarantee_percentage", precision = 5, scale = 2)
    private BigDecimal guaranteePercentage;
}
