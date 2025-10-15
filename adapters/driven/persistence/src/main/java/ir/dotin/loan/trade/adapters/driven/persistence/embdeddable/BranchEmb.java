package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class BranchEmb implements Serializable {

    @Column(name = "branch_code", nullable = false)
    private String code;

    @Column(name = "branch_name", nullable = false)
    private String name;
}
