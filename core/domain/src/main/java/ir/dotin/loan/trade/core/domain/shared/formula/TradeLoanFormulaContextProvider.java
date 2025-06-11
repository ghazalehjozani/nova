package ir.dotin.loan.trade.core.domain.shared.formula;

import java.util.EnumMap;
import java.util.Map;

import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseLoanFormulaContextProvider;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.shared.interaction.TradeLoanArrangementDataProvider;

@DomainService
public final class TradeLoanFormulaContextProvider
        extends BaseLoanFormulaContextProvider<
                BaseFormulaField,
                TradeLoanFacility,
                TradeLoanArrangementId,
                TradeLoanArrangement,
                TradeFieldValueResolver> {

    public TradeLoanFormulaContextProvider(TradeLoanArrangementDataProvider arrangementDataProvider) {
        super(arrangementDataProvider);
    }

    @Override
    protected Map<BaseFormulaField, TradeFieldValueResolver> initializeFieldResolvers() {
        Map<BaseFormulaField, TradeFieldValueResolver> resolvers = new EnumMap<>(BaseFormulaField.class);

        // Sanction-based fields
        resolvers.put(BaseFormulaField.APPROVED_AMOUNT, super::resolveApprovedAmount);

        // Application-based fields
        resolvers.put(BaseFormulaField.REQUESTED_AMOUNT, super::resolveRequestedAmount);

        // Arrangement-based fields
        resolvers.put(BaseFormulaField.BASE_INTEREST_RATE, super::resolveBaseInterestRate);
        resolvers.put(BaseFormulaField.PENALTY_RATE, super::resolvePenaltyRate);
        // TODO: current installment period use max, change to correct
        resolvers.put(BaseFormulaField.INSTALLMENT_PERIOD_DAYS, super::resolveInstallmentPeriodDays);

        // field
        resolvers.put(BaseFormulaField.GUARANTOR_COUNT, super::resolveGuarantorCount);

        // Mixed fields (Sanction or Application)
        resolvers.put(BaseFormulaField.LOAN_DURATION_DAYS, super::resolveLoanDurationDays);
        resolvers.put(BaseFormulaField.GRACE_PERIOD_DAYS, super::resolveGracePeriodDays);
        resolvers.put(BaseFormulaField.INSTALLMENT_COUNT, super::resolveInstallmentCount);

        // TODO: Insurance-related fields - currently placeholders
        resolvers.put(BaseFormulaField.LIFE_INSURANCE_RATE, super::resolveInsuranceField);
        resolvers.put(BaseFormulaField.INSURANCE_COMPANY_SHARE_RATE, super::resolveInsuranceField);
        resolvers.put(BaseFormulaField.VAT_ON_INSURANCE_RATE, super::resolveInsuranceField);
        resolvers.put(BaseFormulaField.LIFE_INSURANCE_DISCOUNT_RATE, super::resolveInsuranceField);
        resolvers.put(BaseFormulaField.GRACE_PERIOD_LIFE_INSURANCE_DISCOUNT_RATE, super::resolveInsuranceField);

        return resolvers;
    }

    @Override
    protected boolean requiresArrangement(BaseFormulaField fieldEnum) {
        return switch (fieldEnum) {
            case BASE_INTEREST_RATE,
                    PENALTY_RATE,
                    LIFE_INSURANCE_RATE,
                    INSURANCE_COMPANY_SHARE_RATE,
                    VAT_ON_INSURANCE_RATE,
                    INSTALLMENT_PERIOD_DAYS,
                    GUARANTOR_COUNT,
                    LIFE_INSURANCE_DISCOUNT_RATE,
                    GRACE_PERIOD_LIFE_INSURANCE_DISCOUNT_RATE -> true;
            default -> false;
        };
    }
}
