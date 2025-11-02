package ir.dotin.loan.trade.core.domain.loantype.specification;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.validation.Specification;
import ir.dotin.loan.baseloan.core.domain.loantype.entity.AbstractLoanType;
import ir.dotin.loan.baseloan.core.domain.loantype.i18n.LoanTypeLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public class MandatoryRelationTypeLoanTopicSpecification implements Specification<AbstractLoanType> {

    private final Set<TradeRelationType> mandatoryRelationTypes;

    public MandatoryRelationTypeLoanTopicSpecification() {
        this.mandatoryRelationTypes = getDefaultMandatoryRelationTypes();
    }

    public MandatoryRelationTypeLoanTopicSpecification(Set<TradeRelationType> mandatoryRelationTypes) {
        this.mandatoryRelationTypes = new HashSet<>(mandatoryRelationTypes);
    }

    @Override
    public Result<Boolean> isSatisfiedBy(AbstractLoanType candidate) {
        Set<TradeRelationType> providedRelationTypes = candidate.getRelationTypeLoanTopics().keySet().stream()
                .map(rt -> (TradeRelationType) rt)
                .collect(Collectors.toSet());

        Set<TradeRelationType> missing = new HashSet<>(mandatoryRelationTypes);
        missing.removeAll(providedRelationTypes);

        if (!missing.isEmpty()) {
            String missingTypes = missing.stream().map(TradeRelationType::name).collect(Collectors.joining(", "));
            return Result.failure(Notification.ofError(
                    LoanTypeLocalizedMessageCodes.MISSING_MANDATORY_RELATION_TYPE_TOPICS, missingTypes));
        }

        return Result.success(true);
    }

    private static Set<TradeRelationType> getDefaultMandatoryRelationTypes() {
        return Set.of(
                TradeRelationType.BANK_COMMITMENTS,
                TradeRelationType.BANK_COMMITMENTS_CONTRA,
                TradeRelationType.PRINCIPAL,
                TradeRelationType.FUTURE_INTEREST);
    }

    public static Set<TradeRelationType> getMandatoryRelationTypesForConditions(TradeLoanType loanType) {
        return getDefaultMandatoryRelationTypes();
    }
}
