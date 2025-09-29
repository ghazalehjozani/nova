package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;

import lombok.Data;

@Data
@Embeddable
public class DisburseDestinationEmb implements Serializable {

    @Column(name = "deposit_number", length = 100)
    private String depositNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "disburse_destination_type", nullable = false, length = 20)
    private DisburseDestinationType type;
}
