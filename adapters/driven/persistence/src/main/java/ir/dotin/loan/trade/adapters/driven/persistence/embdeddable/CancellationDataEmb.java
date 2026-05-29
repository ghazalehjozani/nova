package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class CancellationDataEmb {

    @Nullable
    @Column(name = "cancelDescription", columnDefinition = "TEXT")
    private String cancelDescription;

    @Nullable
    @Column(name = "cancelReason", columnDefinition = "TEXT")
    private String cancelReason;

    @Nullable
    @Column(name = "cancel_date", columnDefinition = "")
    private LocalDate cancelDate;
}
