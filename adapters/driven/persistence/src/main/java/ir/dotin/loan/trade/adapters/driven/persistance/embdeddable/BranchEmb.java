package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class BranchEmb implements Serializable {

    @Column(name = "branch_code", nullable = false, length = 50)
    private String code;

    @Column(name = "branch_name", nullable = false, length = 200)
    private String name;
}
