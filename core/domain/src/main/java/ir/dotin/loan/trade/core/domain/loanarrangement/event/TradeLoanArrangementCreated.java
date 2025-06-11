package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import ir.dotin.platform.domain.common.vo.CurrencyType;
import ir.dotin.platform.domain.common.vo.DurationRange;
import ir.dotin.platform.domain.common.vo.MoneyRange;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.ArrangementDisburseType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.*;
import ir.dotin.loan.baseloan.core.domain.shared.enums.*;
import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;

import static java.util.Objects.requireNonNull;

public record TradeLoanArrangementCreated(
        UUID eventId, TradeLoanArrangementId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanArrangementEvent<TradeLoanArrangementCreated, TradeLoanArrangementCreated.Payload> {

    public static final String CREATED = "CREATED";

    public TradeLoanArrangementCreated {
        requireNonNull(eventId, "eventId cannot be null");
        requireNonNull(aggregateId, "aggregateId cannot be null");
        requireNonNull(payload, "payload cannot be null");
        requireNonNull(createdAt, "createdAt cannot be null");
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + CREATED;
    }

    public record Payload(
            LoanArrangementCode code,
            Title title,
            Set<CurrencyType> currencies,
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
            ConfirmType confirmType,
            boolean active) {
        public Payload {
            requireNonNull(code, "payload.code cannot be null");
            requireNonNull(title, "payload.title cannot be null");
        }
    }
}
