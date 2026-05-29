package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Embeddable
@Data
public class BranchEmb implements Serializable {

    @Nullable
    @Column(name = "branch_code", nullable = false)
    private String code;
}
