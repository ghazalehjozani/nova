package ir.dotin.loan.trade.core.domain.shared.formula;

import java.util.function.Function;

import ir.dotin.platform.commons.domain.vo.ValueType;
import ir.dotin.loan.baseloan.core.domain.shared.formula.LoanFacilityFormulaField;

public enum TradeLoanFacilityFormulaField
        implements LoanFacilityFormulaField<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> {
    APPROVED_AMOUNT(LoanFacilityFormulaField.approvedAmount()) {
        @Override
        Object extract(TradeLoanParameterProvider p) {
            return LoanFacilityFormulaField.approvedAmount().getExtractor().apply(p);
        }
    },

    REQUESTED_AMOUNT(LoanFacilityFormulaField.requestedAmount()) {
        @Override
        Object extract(TradeLoanParameterProvider p) {
            return LoanFacilityFormulaField.requestedAmount().getExtractor().apply(p);
        }
    },

    COMMISSION_AMOUNT(ValueType.MONEY) {
        @Override
        Object extract(TradeLoanParameterProvider p) {
            return p.getCommissionAmount();
        }
    },

    SHIPMENT_VALUE(ValueType.MONEY) {
        @Override
        Object extract(TradeLoanParameterProvider p) {
            return p.getShipmentValue();
        }
    },

    INSURANCE_RATE(ValueType.RATE) {
        @Override
        Object extract(TradeLoanParameterProvider p) {
            return p.getInsuranceRate();
        }
    };

    private final ValueType expectedType;

    TradeLoanFacilityFormulaField(ValueType expectedType) {
        this.expectedType = expectedType;
    }

    TradeLoanFacilityFormulaField(
            LoanFacilityFormulaField<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> base) {
        this.expectedType = base.getExpectedType();
    }

    abstract Object extract(TradeLoanParameterProvider p);

    @Override
    public ValueType getExpectedType() {
        return expectedType;
    }

    @Override
    public Function<TradeLoanParameterProvider, Object> getExtractor() {
        return this::extract;
    }
}
