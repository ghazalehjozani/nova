package ir.dotin.loan.trade.core.domain.shared.formula;

import java.util.function.Function;

import ir.dotin.platform.domain.common.vo.ValueType;
import ir.dotin.loan.baseloan.core.domain.shared.formula.LoanFacilityFormulaField;

public enum TradeLoanFacilityFormulaField
        implements LoanFacilityFormulaField<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> {
    APPROVED_AMOUNT(LoanFacilityFormulaField.approvedAmount()),

    REQUESTED_AMOUNT(LoanFacilityFormulaField.requestedAmount()),

    COMMISSION_AMOUNT(ValueType.MONEY, TradeLoanParameterProvider::getCommissionAmount),

    SHIPMENT_VALUE(ValueType.MONEY, TradeLoanParameterProvider::getShipmentValue),

    INSURANCE_RATE(ValueType.RATE, TradeLoanParameterProvider::getInsuranceRate);

    private final ValueType expectedType;

    @SuppressWarnings("Immutable")
    private final Function<TradeLoanParameterProvider, Object> extractor;

    TradeLoanFacilityFormulaField(ValueType expectedType, Function<TradeLoanParameterProvider, Object> extractor) {
        this.expectedType = expectedType;
        this.extractor = extractor;
    }

    TradeLoanFacilityFormulaField(
            LoanFacilityFormulaField<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>
                    loanFacilityFormulaField) {
        this.expectedType = loanFacilityFormulaField.getExpectedType();
        this.extractor = loanFacilityFormulaField.getExtractor();
    }

    @Override
    public ValueType getExpectedType() {
        return expectedType;
    }

    @Override
    public Function<TradeLoanParameterProvider, Object> getExtractor() {
        return extractor;
    }
}
