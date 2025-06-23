package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.domain.common.annotation.DomainFactory;
import ir.dotin.platform.domain.common.entity.Identity;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractLoanFacilityFactory;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loantype.vo.TradeLoanTypeId;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@DomainFactory
public class TradeLoanFacilityFactory
        extends AbstractLoanFacilityFactory<
                TradeLoanFacility, TradeLoanFacilityId, TradeLoanApplication, TradeSanctionedLoan> {

    public TradeLoanFacilityFactory(Clock clock) {
        super(clock);
    }

    @Override
    protected TradeLoanFacility buildFacility(
            TradeLoanFacilityId id, TradeLoanApplication application, Identity loanArrangementId, Clock systemClock) {

        requireNonNull(id, "Facility ID cannot be null");
        requireNonNull(application, "Application cannot be null");
        requireNonNull(loanArrangementId, "Loan arrangement ID cannot be null");

        checkArgument(
                (loanArrangementId instanceof TradeLoanArrangementId),
                "Expected TradeLoanArrangementId, got: %s",
                loanArrangementId.getClass().getSimpleName());
        TradeLoanArrangementId arrangementId = (TradeLoanArrangementId) loanArrangementId;

        TradeLoanTypeId loanTypeId = TradeLoanTypeId.generate();

        return new TradeLoanFacility(
                id, application, null, FacilityStatus.APPLICATION_SUBMITTED, loanTypeId, arrangementId);
    }

    @Override
    public TradeLoanFacility reconstitute(
            TradeLoanFacilityId id,
            TradeLoanApplication application,
            @Nullable TradeSanctionedLoan sanctionedLoan,
            FacilityStatus status,
            Identity loanArrangementId) {

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

        return new TradeLoanFacility(id, application, sanctionedLoan, status, loanTypeId, arrangementId);
    }
}
