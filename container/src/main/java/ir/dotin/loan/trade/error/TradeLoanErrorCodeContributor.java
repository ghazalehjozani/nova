package ir.dotin.loan.trade.error;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.error.CodedMessage;
import ir.dotin.platform.pangaea.protocol.api.error.ErrorCodeContributor;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.error.TradeLoanFacilityErrors;

/**
 * Contributes all error codes from the Trade Loan product.
 *
 * <p>Registered with {@code @Order(1)} so product codes load after shared kernel, making collision messages point to
 * the trade side.
 *
 * @since 2.0
 */
@Component
@Order(1)
public class TradeLoanErrorCodeContributor implements ErrorCodeContributor {

    @Override
    public Collection<? extends CodedMessage<?>> contributedCodes() {
        var all = new ArrayList<CodedMessage<?>>();
        Collections.addAll(all, CoreBankingErrors.values());
        Collections.addAll(all, TradeLoanApplicationServiceErrors.values());
        Collections.addAll(all, TradeLoanFacilityErrors.values());
        return Collections.unmodifiableList(all);
    }
}
