package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.commandhandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralDetails;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.CollateralValidationContext;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;

final class CollateralAdequacyValidator {

    private static final Logger log = LoggerFactory.getLogger(CollateralAdequacyValidator.class);

    private CollateralAdequacyValidator() {}

    static Result<Unit> validateCollateralAdequacy(List<Collateral> collaterals, CollateralValidationContext context) {
        for (Collateral collateral : collaterals) {
            CollateralDetails details = context.collateralDetailsMap().get(collateral.collateralSerial());
            if (details == null) {
                return Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.COLLATERAL_DETAILS_NOT_FOUND,
                        collateral.collateralSerial().value()));
            }

            Money realCollateralPrice = Money.valueOf(
                            details.price(), context.arrangement().getCurrencyType())
                    .unwrap();

            if (collateral.usedAmount().isGreaterThan(realCollateralPrice).unwrap()) {
                log.warn(
                        "Collateral adequacy validation failed for serial {}",
                        collateral.collateralSerial().value());
                return Result.failure(
                        TradeLoanApplicationServiceErrors.INSUFFICIENT_COLLATERAL_VALUE,
                        realCollateralPrice,
                        collateral.usedAmount());
            }
        }
        return Result.success();
    }

    static Result<Unit> validateTotalCollateralValue(Money totalValue, Money requiredAmount) {
        if (totalValue.isLessThan(requiredAmount).unwrap()) {
            log.warn("Total new collateral value {} is less than required amount {}", totalValue, requiredAmount);
            return Result.failure(
                    TradeLoanApplicationServiceErrors.INSUFFICIENT_COLLATERAL_VALUE, totalValue, requiredAmount);
        }
        return Result.success();
    }
}
