package ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import ir.dotin.platform.adapter.persistence.embeddable.MoneyEmb;
import ir.dotin.platform.adapter.persistence.embeddable.PeriodEmb;
import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.CollateralSerialEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.CurrencyTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.GracePeriodEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.InstallmentCountEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.RevocationReasonEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.SanctionSerialEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade_sanctioned_loan")
public class TradeSanctionedLoanEntity extends PersistentEntity {

    @Embedded
    private SanctionSerialEmb sanctionSerial;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "approved_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "approved_currency", length = 3))
    })
    private MoneyEmb approvedAmount;

    @Embedded
    private CurrencyTypeEmb currency;

    @Embedded
    private GracePeriodEmb gracePeriod;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "value", column = @Column(name = "installment_count"))})
    private InstallmentCountEmb installmentCount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "years", column = @Column(name = "loan_duration_years")),
        @AttributeOverride(name = "months", column = @Column(name = "loan_duration_months")),
        @AttributeOverride(name = "days", column = @Column(name = "loan_duration_days"))
    })
    private PeriodEmb loanDuration;

    @Column(name = "life_insurance_id")
    private String lifeInsuranceId;

    @Embedded
    private CollateralSerialEmb collateralSerial;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "disbursement_schedule_id", nullable = false)
    private DisbursementScheduleEntity disbursementSchedule;

    @Embedded
    private RevocationReasonEmb revocationReason;
}
