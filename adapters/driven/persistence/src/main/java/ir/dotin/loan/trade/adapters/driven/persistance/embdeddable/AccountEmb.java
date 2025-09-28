package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class AccountEmb implements Serializable {

    @Column(name = "account_number", nullable = false, length = 100)
    private String accountNumber;

    @Column(name = "account_holder", nullable = false, length = 200)
    private String accountHolder;

    @Column(name = "bank_code", nullable = false, length = 50)
    private String bankCode;

    @Column(name = "branch_code", length = 50)
    private String branchCode;

    @Column(name = "iban", length = 100)
    private String iban;
}
