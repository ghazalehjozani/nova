package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.commons.domain.vo.Rate;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractLoanFacility;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.LoanFacilityEventFactory;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.shared.vo.*;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityEvents;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

import static java.util.Objects.requireNonNull;

public final class TradeLoanFacility
        extends AbstractLoanFacility<TradeLoanApplication, TradeSanctionedLoan, TradeLoanFacility.Builder>
        implements TradeLoanParameterProvider {

    private TradeLoanFacility(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Factory method for creating a new Trade Loan Facility in APPLICATION_SUBMITTED state */
    public static TradeLoanFacility create(
            LoanFacilityId id,
            TradeLoanApplication application,
            LoanTypeId loanTypeId,
            LoanArrangementId loanArrangementId,
            Clock clock,
            @Nullable InstallmentScheduleId installmentScheduleId) {

        requireNonNull(id, "Facility ID cannot be null");
        requireNonNull(application, "Application cannot be null");
        requireNonNull(loanTypeId, "Loan type ID cannot be null");
        requireNonNull(loanArrangementId, "Loan arrangement ID cannot be null");
        requireNonNull(clock, "Clock cannot be null");

        Builder b = builder()
                .id(id)
                .loanApplication(application)
                .loanTypeId(loanTypeId)
                .loanArrangementId(loanArrangementId)
                .currentState(FacilityStatus.APPLICATION_SUBMITTED);
        if (installmentScheduleId != null) {
            b.installmentScheduleId(installmentScheduleId);
        }
        TradeLoanFacility facility = b.buildInternal();

        var createdEvent = facility.getEventFactory()
                .createCreatedEvent(
                        facility.getId(),
                        facility.getLoanApplication().getApplicationNumber().orElseThrow(),
                        clock);
        facility.registerEvent(createdEvent);

        return facility;
    }

    @Override
    public String getFacilityType() {
        return "TRADE";
    }

    @Override
    protected LoanFacilityEventFactory<TradeLoanFacilityEvents<?>> createEventFactory() {
        return new TradeLoanFacilityEventFactory();
    }

    public Money getCommissionAmount() {
        return getSanctionedLoan()
                .map(sanctioned -> Money.zero(sanctioned.getCurrency()).unwrap())
                .orElseGet(() -> Money.zero(getLoanApplication().getCurrency()).unwrap()); // Default implementation
    }

    public Money getShipmentValue() {
        return getSanctionedLoan()
                .map(sanctioned -> Money.zero(sanctioned.getCurrency()).unwrap())
                .orElseGet(() -> Money.zero(getLoanApplication().getCurrency()).unwrap()); // Default implementation
    }

    public Rate getInsuranceRate() {
        return Rate.valueOf(0.0).unwrap(); // Default implementation - should be overridden by business logic
    }

    public static final class Builder
            extends AbstractLoanFacility.AbstractLoanFacilityBuilder<
                    TradeLoanApplication, TradeSanctionedLoan, TradeLoanFacility, TradeRelationType, Builder> {

        public Builder() {}

        @Override
        public TradeLoanFacility buildInternal() {
            return new TradeLoanFacility(this);
        }

        @Override
        protected void validateSpecificRules(@NonNull Notification notification) {}

        @Override
        public Builder loanApplication(@NonNull TradeLoanApplication newLoanApplication) {
            super.loanApplication(newLoanApplication);
            return this;
        }

        @Override
        public Builder sanctionedLoan(@Nullable TradeSanctionedLoan newSanctionedLoan) {
            super.sanctionedLoan(newSanctionedLoan);
            return this;
        }

        @Override
        public Builder issueContractTransactionNumbers(@Nullable List<TrackedTransactionNumber> newTransactionNumbers) {
            super.issueContractTransactionNumbers(newTransactionNumbers);
            return this;
        }

        @Override
        public Builder disbursementTransactionNumbers(@Nullable List<TrackedTransactionNumber> newTransactionNumbers) {
            super.disbursementTransactionNumbers(newTransactionNumbers);
            return this;
        }
    }
}
