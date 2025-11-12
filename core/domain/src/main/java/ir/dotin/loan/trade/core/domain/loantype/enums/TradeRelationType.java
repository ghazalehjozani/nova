package ir.dotin.loan.trade.core.domain.loantype.enums;

import ir.dotin.platform.commons.core.i18n.LocalizedEnum;
import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.Direction;

@SuppressWarnings("CheckStyle")
public enum TradeRelationType implements RelationType<TradeRelationType>, LocalizedEnum<TradeRelationType> {

    // --- Core Components ---
    PRINCIPAL(Direction.DEBIT), // اصلی
    FUTURE_INTEREST(Direction.CREDIT), // سود سال های آینده
    PENALTY(Direction.DEBIT), // جریمه
    RECEIVED_INTEREST(Direction.CREDIT), // سود دریافتی (Assuming CREDIT side for received)

    // --- Receivables Status (based on aging/regulation) ---
    RECEIVABLES_PAST_DUE(Direction.DEBIT), // مطالبات بعد از سررسید
    RECEIVABLES_OVERDUE(Direction.DEBIT), // مطالبات سررسید گذشته
    RECEIVABLES_DOUBTFUL(Direction.DEBIT), // مطالبات معوق (Often 'Doubtful' in IFRS terms)
    RECEIVABLES_SUBSTANDARD(Direction.DEBIT), // مطالبات مشکوک الوصول (Substandard/Doubtful)
    RECEIVABLES_WRITTEN_OFF(Direction.DEBIT), // مطالبات سوخت شده

    // --- Accruals & Provisions ---
    INTEREST_SHORTFALL_PROVISION(Direction.CREDIT), // تامین کسری سود
    ACCRUED_INTEREST(Direction.DEBIT), // سود تعهدي
    DEFERRED_INTEREST(Direction.DEBIT), // سود معوق (Interest recognized but payment delayed)
    ACCRUED_DEFERRED_INTEREST(Direction.DEBIT), // سود معوق تعهدی

    // --- Commitments ---
    BANK_COMMITMENTS(Direction.CREDIT), // تعهدات بانک
    BANK_COMMITMENTS_CONTRA(Direction.DEBIT), // طرف تعهدات بانک

    // --- Specific Receivable Interest/Penalty Types (Accrued/Deferred) ---
    RECEIVABLES_ACCRUED_DEFERRED_INTEREST(Direction.DEBIT), // سود معوق تعهدی مطالبات
    RECEIVABLES_DEFERRED_INTEREST(Direction.DEBIT), // سود معوق مطالبات
    RECEIVABLES_FUTURE_INTEREST(Direction.CREDIT), // سود سررسید آتی مطالبات
    ACCRUED_PENALTY(Direction.DEBIT), // جریمه تعهدی
    RECEIVABLES_ACCRUED_PENALTY(Direction.DEBIT), // جریمه تعهدی مطالبات
    RECEIVABLES_PENALTY(Direction.DEBIT), // جریمه مطالبات
    CURRENT_DEBT_RECEIVABLES(Direction.DEBIT), // مطالبات دین حال

    // --- Contextual/Metadata Relations ---
    DISBURSEMENT_TRANSACTION_CONTEXT(Direction.CREDIT); // زمینه تراکنش پرداخت (Disbursement transaction context)

    private final Direction direction;

    TradeRelationType(Direction direction) {
        this.direction = direction;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public boolean isContextual() {
        return this == DISBURSEMENT_TRANSACTION_CONTEXT;
    }
}
