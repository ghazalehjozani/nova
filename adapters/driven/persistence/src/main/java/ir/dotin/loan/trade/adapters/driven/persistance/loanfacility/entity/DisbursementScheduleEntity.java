package ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.entity;

import java.util.List;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;

import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.ScheduledTrancheEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class DisbursementScheduleEntity extends PersistentEntity {

    @ElementCollection
    @CollectionTable(
            name = "disbursement_schedule_tranches",
            joinColumns = @JoinColumn(name = "disbursement_schedule_id"))
    private List<ScheduledTrancheEmb> tranches;
}
