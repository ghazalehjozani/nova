package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ScheduleHistoryEmb implements Serializable {

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "loan_installment_schedule_history_ids",
            joinColumns = @JoinColumn(name = "current_schedule_id"))
    @Column(name = "previous_schedule_id")
    @OrderColumn(name = "sequence_order")
    private List<UUID> previousScheduleIds = new ArrayList<>();
}
