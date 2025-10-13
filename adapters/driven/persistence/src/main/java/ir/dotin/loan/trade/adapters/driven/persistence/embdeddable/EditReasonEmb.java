package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class EditReasonEmb implements Serializable {
    @Column(name = "edit_reason", nullable = false, length = 500)
    private String editReason;
}
