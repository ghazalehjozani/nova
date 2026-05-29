package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class EditReasonEmb implements Serializable {

    @Nullable
    @Column(name = "edit_reason", length = 500)
    private String editReason;
}
