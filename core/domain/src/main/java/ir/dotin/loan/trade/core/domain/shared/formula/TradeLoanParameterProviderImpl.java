package ir.dotin.loan.trade.core.domain.shared.formula;

import java.time.Period;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static java.util.Objects.requireNonNull;

public final class TradeLoanParameterProviderImpl implements TradeLoanParameterProvider {

    private final TradeLoanFacility tradeLoanFacility;

    public TradeLoanParameterProviderImpl(TradeLoanFacility tradeLoanFacility) {
        this.tradeLoanFacility = requireNonNull(tradeLoanFacility, "tradeLoanFacility cannot be null");
    }

    public static TradeLoanParameterProviderImpl of(TradeLoanFacility tradeLoanFacility) {
        return new TradeLoanParameterProviderImpl(tradeLoanFacility);
    }

    @Override
    public Money getApprovedAmount() {
        return tradeLoanFacility.getSanctionedLoan().orElseThrow().getApprovedAmount();
    }

    @Override
    public Money getRequestedAmount() {
        return tradeLoanFacility.getLoanApplication().getRequestedAmount();
    }

    @Override
    public Period getRequestedDuration() {
        return tradeLoanFacility.getSanctionedLoan().orElseThrow().getLoanDuration() != null
                ? tradeLoanFacility
                        .getSanctionedLoan()
                        .orElseThrow()
                        .getLoanDuration()
                        .value()
                : Period.ZERO;
    }

    @Override
    public Period getGracePeriod() {
        return tradeLoanFacility.getSanctionedLoan().orElseThrow().getGracePeriod() != null
                ? tradeLoanFacility
                        .getSanctionedLoan()
                        .orElseThrow()
                        .getGracePeriod()
                        .value()
                : Period.ZERO;
    }

    @Override
    public Integer getInstallmentCount() {
        return tradeLoanFacility.getSanctionedLoan().orElseThrow().getInstallmentCount() != null
                ? tradeLoanFacility
                        .getSanctionedLoan()
                        .orElseThrow()
                        .getInstallmentCount()
                        .value()
                : null;
    }

    @Override
    public Integer getGuarantorCount() {
        return Math.toIntExact(
                tradeLoanFacility.getLoanApplication().getGuarantors().size());
    }

    public TradeLoanFacility getTradeLoanFacility() {
        return tradeLoanFacility;
    }
}
