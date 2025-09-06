package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.domain.common.entity.Identity;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractLoanFacility;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.LoanFacilityEventFactory;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityEvent;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public final class TradeLoanFacility extends AbstractLoanFacility<TradeLoanApplication, TradeSanctionedLoan> {

    private final LoanTypeId loanTypeId;
    private final LoanArrangementId loanArrangementId;
    private final Money totalTradeDisbursementAmount;

    TradeLoanFacility(
            LoanFacilityId id,
            TradeLoanApplication application,
            @Nullable TradeSanctionedLoan sanctionedLoan,
            FacilityStatus status,
            LoanTypeId loanTypeId,
            LoanArrangementId loanArrangementId,
            Money totalDisbursementAmount) {
        super(id, application, sanctionedLoan, status, loanArrangementId, totalDisbursementAmount);
        this.loanTypeId = requireNonNull(loanTypeId, "LoanTypeId cannot be null");
        this.loanArrangementId = requireNonNull(loanArrangementId, "LoanArrangementId cannot be null");
        this.totalTradeDisbursementAmount = totalDisbursementAmount;
    }

    @Override
    protected void validateInternalState() {
        super.validateInternalState();
    }

    public static TradeLoanFacility create(
            LoanFacilityId id,
            TradeLoanApplication application,
            Identity loanArrangementId,
            Money totalDisbursementAmount,
            Clock clock) {

        requireNonNull(id, "Facility ID cannot be null");
        requireNonNull(application, "Application cannot be null");
        requireNonNull(loanArrangementId, "Loan arrangement ID cannot be null");

        checkArgument(
                (loanArrangementId instanceof LoanArrangementId),
                "Expected LoanArrangementId, got: %s",
                loanArrangementId.getClass().getSimpleName());
        LoanArrangementId arrangementId = (LoanArrangementId) loanArrangementId;

        LoanTypeId typeId = new LoanTypeId(randomUUID());

        TradeLoanFacility facility = new TradeLoanFacility(
                id,
                application,
                null,
                FacilityStatus.APPLICATION_SUBMITTED,
                typeId,
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
            LoanFacilityId id,
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
                (loanArrangementId instanceof LoanArrangementId),
                "Expected LoanArrangementId, got: %s",
                loanArrangementId.getClass().getSimpleName());
        LoanArrangementId arrangementId = (LoanArrangementId) loanArrangementId;

        LoanTypeId typeId = new LoanTypeId(randomUUID());

        return new TradeLoanFacility(
                id, application, sanctionedLoan, status, typeId, arrangementId, totalDisbursementAmount);
    }

    @Override
    public LoanTypeId getLoanTypeId() {
        return loanTypeId;
    }

    @Override
    public LoanArrangementId getLoanArrangementId() {
        return loanArrangementId;
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
