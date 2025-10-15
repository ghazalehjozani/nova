package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class AccountEmb implements Serializable {

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "account_holder", nullable = false)
    private String accountHolder;

    @Column(name = "bank_code", nullable = false)
    private String bankCode;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "iban")
    private String iban;
}
