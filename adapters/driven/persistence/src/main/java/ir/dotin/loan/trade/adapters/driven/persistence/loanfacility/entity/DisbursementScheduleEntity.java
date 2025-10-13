package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity;

import java.util.List;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ScheduledTrancheEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_disbursement_schedules")
@Setter
@Getter
@NoArgsConstructor
public class DisbursementScheduleEntity extends PersistentEntity {

    @ElementCollection
    @CollectionTable(
            name = "loan_disbursement_schedule_tranches",
            joinColumns = @JoinColumn(name = "disbursement_schedule_id"))
    private List<ScheduledTrancheEmb> tranches;
}
