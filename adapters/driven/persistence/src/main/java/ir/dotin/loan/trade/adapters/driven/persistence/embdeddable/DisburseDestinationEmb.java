package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;

import lombok.Data;

@Data
@Embeddable
public class DisburseDestinationEmb implements Serializable {

    @Nullable
    @Column(name = "deposit_number")
    private String depositNumber;

    @Nullable
    @Column(name = "account_number")
    private String accountNumber;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "disburse_destination_type", nullable = false)
    private DisburseDestinationType type;
}
