package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CollectionTable;
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
public class DisbursementHistoryEmb {

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "loan_disbursement_records", joinColumns = @JoinColumn(name = "loan_facility_id"))
    @OrderColumn(name = "record_order")
    private List<DisbursementRecordEmb> records = new ArrayList<>();
}
