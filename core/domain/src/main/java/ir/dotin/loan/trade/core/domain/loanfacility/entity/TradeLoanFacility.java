package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractLoanFacility;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.LoanFacilityEventFactory;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DestinationAccount;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumbers;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityEvent;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;

public final class TradeLoanFacility extends AbstractLoanFacility<TradeLoanApplication, TradeSanctionedLoan> {

    public TradeLoanFacility(Builder builder) {
        super(
                builder.id,
                builder.loanApplication,
                builder.loanTypeId,
                builder.loanArrangementId,
                builder.sanctionedLoan,
                builder.currentState,
                builder.issueContractTransactionNumbers,
                builder.disbursementTransactionNumbers,
                builder.disbursementDestinationAccount,
                builder.totalDisbursedAmount);
    }

    public static Builder builder() {
        return new Builder();
    }

    private TradeLoanFacility(
            LoanFacilityId id,
            TradeLoanApplication application,
            FacilityStatus status,
            LoanTypeId loanTypeId,
            LoanArrangementId loanArrangementId,
            Money totalDisbursedAmount) {
        super(id, application, null, status, loanTypeId, loanArrangementId, totalDisbursedAmount);
    }

    @Override
    protected void validateInternalState() {
        super.validateInternalState();
    }

    @SuppressWarnings("unchecked")
    @Override
    public TrackedTransactionNumbers<TradeRelationType> getIssueContractTransactionNumbers() {
        return (TrackedTransactionNumbers<TradeRelationType>) super.issueContractTransactionNumbers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TrackedTransactionNumbers<TradeRelationType> getDisbursementTransactionNumbers() {
        return (TrackedTransactionNumbers<TradeRelationType>) super.disbursementTransactionNumbers;
    }

    public static TradeLoanFacility create(
            LoanFacilityId id,
            TradeLoanApplication application,
            LoanTypeId loanTypeId,
            LoanArrangementId loanArrangementId,
            Money totalDisbursementAmount,
            Clock clock) {

        requireNonNull(id, "Facility ID cannot be null");
        requireNonNull(application, "Application cannot be null");
        requireNonNull(loanArrangementId, "Loan arrangement ID cannot be null");

        TradeLoanFacility facility = new TradeLoanFacility(
                id,
                application,
                FacilityStatus.APPLICATION_SUBMITTED,
                loanTypeId,
                loanArrangementId,
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

    @Override
    public String getFacilityType() {
        return "TRADE";
    }

    @Override
    protected LoanFacilityEventFactory<TradeLoanFacilityEvent<?, ?>> createEventFactory() {
        return new TradeLoanFacilityEventFactory();
    }

    public static final class Builder {
        @Nullable
        private LoanFacilityId id;

        @Nullable
        private Money totalDisbursedAmount;

        @Nullable
        private DestinationAccount disbursementDestinationAccount;

        @Nullable
        private TrackedTransactionNumbers<TradeRelationType> disbursementTransactionNumbers;

        @Nullable
        private TrackedTransactionNumbers<TradeRelationType> issueContractTransactionNumbers;

        @Nullable
        private FacilityStatus currentState;

        @Nullable
        private TradeSanctionedLoan sanctionedLoan;

        @Nullable
        private LoanArrangementId loanArrangementId;

        @Nullable
        private LoanTypeId loanTypeId;

        @Nullable
        private TradeLoanApplication loanApplication;

        public Builder() {}

        public Builder totalDisbursedAmount(Money totalDisbursedAmount) {
            this.totalDisbursedAmount = totalDisbursedAmount;
            return this;
        }

        public Builder disbursementDestinationAccount(DestinationAccount disbursementDestinationAccount) {
            this.disbursementDestinationAccount = disbursementDestinationAccount;
            return this;
        }

        public Builder disbursementTransactionNumbers(
                TrackedTransactionNumbers<TradeRelationType> disbursementTransactionNumbers) {
            this.disbursementTransactionNumbers = disbursementTransactionNumbers;
            return this;
        }

        public Builder issueContractTransactionNumbers(
                TrackedTransactionNumbers<TradeRelationType> issueContractTransactionNumbers) {
            this.issueContractTransactionNumbers = issueContractTransactionNumbers;
            return this;
        }

        public Builder currentState(FacilityStatus currentState) {
            this.currentState = currentState;
            return this;
        }

        public Builder sanctionedLoan(TradeSanctionedLoan sanctionedLoan) {
            this.sanctionedLoan = sanctionedLoan;
            return this;
        }

        public Builder loanArrangementId(LoanArrangementId loanArrangementId) {
            this.loanArrangementId = loanArrangementId;
            return this;
        }

        public Builder loanTypeId(LoanTypeId loanTypeId) {
            this.loanTypeId = loanTypeId;
            return this;
        }

        public Builder loanApplication(TradeLoanApplication loanApplication) {
            this.loanApplication = loanApplication;
            return this;
        }

        public Builder id(LoanFacilityId id) {
            this.id = id;
            return this;
        }

        public TradeLoanFacility buildInternal() {
            return new TradeLoanFacility(this);
        }
    }
}
