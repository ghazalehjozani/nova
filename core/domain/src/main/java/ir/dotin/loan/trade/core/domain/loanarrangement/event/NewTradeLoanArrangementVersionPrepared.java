package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.domain.common.vo.CurrencyType;
import ir.dotin.platform.domain.common.vo.DurationRange;
import ir.dotin.platform.domain.common.vo.MoneyRange;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.*;
import ir.dotin.loan.baseloan.core.domain.shared.enums.*;
import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public record NewTradeLoanArrangementVersionPrepared(
        UUID eventId, TradeLoanArrangementId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanArrangementEvent<
                NewTradeLoanArrangementVersionPrepared, NewTradeLoanArrangementVersionPrepared.Payload> {

    public NewTradeLoanArrangementVersionPrepared {
        requireNonNull(eventId, "eventId cannot be null");
        requireNonNull(aggregateId, "aggregateId cannot be null (should be new version ID)");
        requireNonNull(payload, "payload cannot be null");
        requireNonNull(createdAt, "createdAt cannot be null");
        checkArgument(aggregateId.equals(payload.newAggregateId()), "aggregateId must match payload.newAggregateId");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "VERSION_PREPARED";
    }

    public record Payload(
            TradeLoanArrangementId newAggregateId,
            TradeLoanArrangementId previousAggregateId,
            LoanArrangementCode code,
            Title title,
            Set<CurrencyType> currencies,
            MoneyRange amountRange,
            DurationRange durationRange,
            PartyType partyType,
            LifeInsurancePaymentType lifeInsurancePaymentType,
            LoanSecondaryType loanSecondaryType,
            SectionType sectionType,
            InterestPolicy<BaseFormulaField> interestPolicy,
            PenaltyPolicy<BaseFormulaField> penaltyPolicy,
            InstallmentPolicy<BaseFormulaField> installmentPolicy,
            GracePeriodPolicy<BaseFormulaField> gracePeriodPolicy,
            RepaymentPriorityPolicy repaymentPriorityPolicy,
            RegulatoryCompliancePolicy regulatoryCompliancePolicy,
            CollateralPolicy collateralPolicy,
            @Nullable Integer guarantorCount,
            boolean hasInstallmentCard,
            @Nullable ConfirmType confirmType,
            @Nullable DisbursementMethod disbursementMethod) {
        public Payload {
            requireNonNull(newAggregateId, "payload.newAggregateId cannot be null");
            requireNonNull(previousAggregateId, "payload.previousAggregateId cannot be null");
            requireNonNull(code, "payload.code cannot be null");
            requireNonNull(title, "payload.title cannot be null");
        }
    }
}
