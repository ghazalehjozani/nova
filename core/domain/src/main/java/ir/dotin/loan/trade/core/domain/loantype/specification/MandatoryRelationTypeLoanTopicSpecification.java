package ir.dotin.loan.trade.core.domain.loantype.specification;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.validation.Specification;
import ir.dotin.loan.baseloan.core.domain.loantype.entity.AbstractLoanType;
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

        //        if (!missing.isEmpty()) {
        //            String missingTypes = missing.stream().map(TradeRelationType::name).collect(Collectors.joining(",
        // "));
        //            return Result.failure(Notification.ofError(
        //                    LoanTypeLocalizedMessageCodes.MISSING_MANDATORY_RELATION_TYPE_TOPICS, missingTypes));
        //        }

        return Result.success(true);
    }

    private static Set<TradeRelationType> getDefaultMandatoryRelationTypes() {
        return Set.of(
                TradeRelationType.RECEIVABLES_DOUBTFUL, // مطالبات معوق
                TradeRelationType.RECEIVABLES_SUBSTANDARD, // مطالبات مشکوک الوصول
                TradeRelationType.RECEIVABLES_ACCRUED_DEFERRED_INTEREST, // سود معوق تعهدی مطالبات
                TradeRelationType.RECEIVABLES_DEFERRED_INTEREST, // سود معوق مطالبات
                TradeRelationType.RECEIVABLES_PENALTY, // جریمه مطالبات
                TradeRelationType.RECEIVABLES_ACCRUED_PENALTY, // جریمه تعهدی مطالبات
                TradeRelationType.ACCRUED_INTEREST, // سود تعهدي
                TradeRelationType.ACCRUED_PENALTY, // جریمه تعهدی
                TradeRelationType.DEFERRED_INTEREST, // سود معوق
                TradeRelationType.ACCRUED_DEFERRED_INTEREST, // سود معوق تعهدی
                TradeRelationType.RECEIVABLES_WRITTEN_OFF, // مطالبات سوخت شده
                TradeRelationType.BANK_COMMITMENTS_CONTRA, // طرف تعهدات بانک
                TradeRelationType.BANK_COMMITMENTS, // تعهدات بانک
                TradeRelationType.RECEIVABLES_FUTURE_INTEREST, // سود سررسید آتی مطالبات
                TradeRelationType.INTEREST_SHORTFALL_PROVISION, // تامین کسری سود
                TradeRelationType.PRINCIPAL, // اصلی
                TradeRelationType.FUTURE_INTEREST, // سود سال های آینده
                TradeRelationType.RECEIVED_INTEREST, // سود دریافتی
                TradeRelationType.PENALTY, // جریمه
                TradeRelationType.RECEIVABLES_PAST_DUE, // مطالبات بعد از سررسید
                TradeRelationType.RECEIVABLES_OVERDUE, // مطالبات سررسید گذشته
                TradeRelationType.DISCOUNT // تخفیف
                );
    }

    public static Set<TradeRelationType> getMandatoryRelationTypesForConditions(TradeLoanType loanType) {
        return getDefaultMandatoryRelationTypes();
    }
}
