package ir.dotin.loan.trade.adapters.driven.persistence.loantype.projection;

/** Projection for {@link ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity} */
public interface TradeLoanTypeCode {
    LoanTypeCodeEmbInfo getCode();

    /** Projection for {@link ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.LoanTypeCodeEmb} */
    interface LoanTypeCodeEmbInfo {
        String getValue();
    }
}
