package ir.dotin.loan.morabehe.core.domain.loanarrangement.event;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import ir.dotin.platform.domain.common.vo.Currency;
import ir.dotin.platform.domain.common.vo.DurationRange;
import ir.dotin.platform.domain.common.vo.MoneyRange;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.ArrangementDisburseType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.*;
import ir.dotin.loan.baseloan.core.domain.shared.enums.*;
import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.vo.MorabeheLoanArrangementId;

public record NewMorabeheLoanArrangementVersionPrepared(
        UUID eventId, MorabeheLoanArrangementId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanArrangementEvent<
                NewMorabeheLoanArrangementVersionPrepared, NewMorabeheLoanArrangementVersionPrepared.Payload> {

    public NewMorabeheLoanArrangementVersionPrepared {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null (should be new version ID)");
        Objects.requireNonNull(payload, "payload cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
        if (!aggregateId.equals(payload.newAggregateId())) {
            throw new IllegalArgumentException("aggregateId must match payload.newAggregateId");
        }
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "VERSION_PREPARED";
    }

    public record Payload(
            MorabeheLoanArrangementId newAggregateId,
            MorabeheLoanArrangementId previousAggregateId,
            LoanRuleCode code,
            Title title,
            Set<Currency> currencies,
            MoneyRange amountRange,
            DurationRange durationRange,
            PartyType partyType,
            DisburseType disburseType,
            LifeInsurancePaymentType lifeInsurancePaymentType,
            ArrangementDisburseType ruleDisburseType,
            LoanSecondaryType loanSecondaryType,
            SectionType sectionType,
            InterestPolicy<BaseFormulaField> interestPolicy,
            PenaltyPolicy<BaseFormulaField> penaltyPolicy,
            InstallmentPolicy<BaseFormulaField> installmentPolicy,
            GracePeriodPolicy<BaseFormulaField> gracePeriodPolicy,
            RepaymentPriorityPolicy repaymentPriorityPolicy,
            RegulatoryCompliancePolicy regulatoryCompliancePolicy,
            CollateralPolicy collateralPolicy,
            Integer guarantorCount,
            boolean hasInstallmentCard,
            ConfirmType confirmType) {
        public Payload {
            Objects.requireNonNull(newAggregateId, "payload.newAggregateId cannot be null");
            Objects.requireNonNull(previousAggregateId, "payload.previousAggregateId cannot be null");
            Objects.requireNonNull(code, "payload.code cannot be null");
            Objects.requireNonNull(title, "payload.title cannot be null");
        }
    }
}
