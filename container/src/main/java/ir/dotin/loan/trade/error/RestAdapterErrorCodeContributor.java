package ir.dotin.loan.trade.error;

import java.util.Collection;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.error.CodedMessage;
import ir.dotin.platform.pangaea.protocol.api.error.ErrorCodeContributor;
import ir.dotin.platform.pangaea.protocol.rest.error.ExceptionMessageCodes;

/**
 * Contributes REST adapter error codes (HTTP request, security, idempotency).
 *
 * @since 2.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class RestAdapterErrorCodeContributor implements ErrorCodeContributor { // TODO: Move to platform

    @Override
    public Collection<? extends CodedMessage<?>> contributedCodes() {
        return List.of(ExceptionMessageCodes.values());
    }
}
