package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.domain.common.entity.Identity;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractLoanFacility;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.LoanFacilityEventFactory;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityEvent;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loantype.vo.TradeLoanTypeId;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public final class TradeLoanFacility
        extends AbstractLoanFacility<TradeLoanFacilityId, TradeLoanApplication, TradeSanctionedLoan> {

    private final TradeLoanTypeId tradeLoanTypeId;
    private final TradeLoanArrangementId tradeLoanArrangementId;
    private final Money totalTradeDisbursementAmount;

    TradeLoanFacility(
            TradeLoanFacilityId id,
            TradeLoanApplication application,
            @Nullable TradeSanctionedLoan sanctionedLoan,
            FacilityStatus status,
            TradeLoanTypeId loanTypeId,
            TradeLoanArrangementId loanArrangementId,
            Money totalDisbursementAmount) {
        super(id, application, sanctionedLoan, status, loanArrangementId, totalDisbursementAmount);
        this.tradeLoanTypeId = requireNonNull(loanTypeId, "tradeLoanTypeId cannot be null");
        this.tradeLoanArrangementId = requireNonNull(loanArrangementId, "tradeLoanArrangementId cannot be null");
        this.totalTradeDisbursementAmount = totalDisbursementAmount;
    }

    @Override
    protected void validateInternalState() {
        super.validateInternalState();
    }

    public static TradeLoanFacility create(
            TradeLoanFacilityId id,
            TradeLoanApplication application,
            Identity loanArrangementId,
            Money totalDisbursementAmount,
            Clock clock) {

        requireNonNull(id, "Facility ID cannot be null");
        requireNonNull(application, "Application cannot be null");
        requireNonNull(loanArrangementId, "Loan arrangement ID cannot be null");

        checkArgument(
                (loanArrangementId instanceof TradeLoanArrangementId),
                "Expected TradeLoanArrangementId, got: %s",
                loanArrangementId.getClass().getSimpleName());
        TradeLoanArrangementId arrangementId = (TradeLoanArrangementId) loanArrangementId;

        TradeLoanTypeId loanTypeId = TradeLoanTypeId.generate();

        TradeLoanFacility facility = new TradeLoanFacility(
                id,
                application,
                null,
                FacilityStatus.APPLICATION_SUBMITTED,
                loanTypeId,
                arrangementId,
                totalDisbursementAmount);

        var createdEvent = facility.getEventFactory()
                .createCreatedEvent(
                        facility.getId(),
                        facility.getLoanApplication().getId(),
                        facility.getLoanApplication().getCustomer(),
                        clock);
        facility.registerEvent(createdEvent);
        return facility;
    }

    public static TradeLoanFacility reconstitute(
            TradeLoanFacilityId id,
            TradeLoanApplication application,
            @Nullable TradeSanctionedLoan sanctionedLoan,
            FacilityStatus status,
            Identity loanArrangementId,
            Money totalDisbursementAmount) {

        requireNonNull(id, "Facility ID cannot be null");
        requireNonNull(application, "Application cannot be null");
        requireNonNull(status, "Facility status cannot be null");
        requireNonNull(loanArrangementId, "Loan arrangement ID cannot be null");

        // For trade loans, we expect specific types
        checkArgument(
                (loanArrangementId instanceof TradeLoanArrangementId),
                "Expected TradeLoanArrangementId, got: %s",
                loanArrangementId.getClass().getSimpleName());
        TradeLoanArrangementId arrangementId = (TradeLoanArrangementId) loanArrangementId;

        TradeLoanTypeId loanTypeId = TradeLoanTypeId.generate();

        return new TradeLoanFacility(
                id, application, sanctionedLoan, status, loanTypeId, arrangementId, totalDisbursementAmount);
    }

    @Override
    public TradeLoanTypeId getLoanTypeId() {
        return tradeLoanTypeId;
    }

    @Override
    public TradeLoanArrangementId getLoanArrangementId() {
        return tradeLoanArrangementId;
    }

    @Override
    public String getLoanFacilityType() {
        return "TRADE";
    }

    @Override
    public Money getTotalDisbursedAmount() {
        return totalTradeDisbursementAmount;
    }

    @Override
    protected LoanFacilityEventFactory<TradeLoanFacilityEvent<?, ?>> createEventFactory() {
        return new TradeLoanFacilityEventFactory();
    }
}
