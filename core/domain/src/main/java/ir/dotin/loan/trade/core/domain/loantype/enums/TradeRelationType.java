package ir.dotin.loan.trade.core.domain.loantype.enums;

import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.Direction;

public enum TradeRelationType implements RelationType<TradeRelationType> {

    // --- Core Components ---
    PRINCIPAL("Principal", Direction.DEBTOR), // اصلی
    FUTURE_INTEREST("Future Interest", Direction.CREDITOR), // سود سال های آینده
    PENALTY("Penalty", Direction.DEBTOR), // جریمه
    RECEIVED_INTEREST("Received Interest", Direction.CREDITOR), // سود دریافتی (Assuming CREDIT side for received)

    // --- Receivables Status (based on aging/regulation) ---
    RECEIVABLES_PAST_DUE("Past Due Receivables", Direction.DEBTOR), // مطالبات بعد از سررسید
    RECEIVABLES_OVERDUE("Overdue Receivables", Direction.DEBTOR), // مطالبات سررسید گذشته
    RECEIVABLES_DOUBTFUL("Doubtful Receivables", Direction.DEBTOR), // مطالبات معوق (Often 'Doubtful' in IFRS terms)
    RECEIVABLES_SUBSTANDARD("Substandard Receivables", Direction.DEBTOR), // مطالبات مشکوک الوصول (Substandard/Doubtful)
    RECEIVABLES_WRITTEN_OFF("Written-off Receivables", Direction.DEBTOR), // مطالبات سوخت شده

    // --- Accruals & Provisions ---
    INTEREST_SHORTFALL_PROVISION("Interest Shortfall Provision", Direction.CREDITOR), // تامین کسری سود
    ACCRUED_INTEREST("Accrued Interest", Direction.DEBTOR), // سود تعهدي
    DEFERRED_INTEREST("Deferred Interest", Direction.DEBTOR), // سود معوق (Interest recognized but payment delayed)
    ACCRUED_DEFERRED_INTEREST("Accrued Deferred Interest", Direction.DEBTOR), // سود معوق تعهدی

    // --- Commitments ---
    BANK_COMMITMENTS("Bank Commitments", Direction.CREDITOR), // تعهدات بانک
    BANK_COMMITMENTS_CONTRA("Bank Commitments Contra", Direction.DEBTOR), // طرف تعهدات بانک

    // --- Specific Receivable Interest/Penalty Types (Accrued/Deferred) ---
    RECEIVABLES_ACCRUED_DEFERRED_INTEREST(
            "Receivables Accrued Deferred Interest", Direction.DEBTOR), // سود معوق تعهدی مطالبات
    RECEIVABLES_DEFERRED_INTEREST("Receivables Deferred Interest", Direction.DEBTOR), // سود معوق مطالبات
    RECEIVABLES_FUTURE_INTEREST("Receivables Future Interest", Direction.CREDITOR), // سود سررسید آتی مطالبات
    ACCRUED_PENALTY("Accrued Penalty", Direction.DEBTOR), // جریمه تعهدی
    RECEIVABLES_ACCRUED_PENALTY("Receivables Accrued Penalty", Direction.DEBTOR), // جریمه تعهدی مطالبات
    RECEIVABLES_PENALTY("Receivables Penalty", Direction.DEBTOR), // جریمه مطالبات

    // --- Other ---
    CURRENT_DEBT_RECEIVABLES("Current Debt Receivables", Direction.DEBTOR); // مطالبات دین حال

    private final String localizableName;
    private final Direction direction;

    TradeRelationType(String localizableName, Direction direction) {
        this.localizableName = localizableName;
        this.direction = direction;
    }

    public String getLocalizableName() {
        return localizableName;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }
}
