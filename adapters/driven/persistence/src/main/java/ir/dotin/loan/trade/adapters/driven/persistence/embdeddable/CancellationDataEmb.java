package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class CancellationDataEmb {
    @Column(name = "cancelDescription", columnDefinition = "TEXT")
    private String cancelDescription;

    @Column(name = "cancelReason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "cancel_date", columnDefinition = "")
    private LocalDate cancelDate;
}
