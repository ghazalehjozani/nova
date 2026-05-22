package ir.dotin.loan.trade.error;

import java.util.Collection;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.error.CodedMessage;
import ir.dotin.platform.pangaea.protocol.api.error.ErrorCodeContributor;
import ir.dotin.platform.pangaea.saga.api.error.SagaErrors;

/**
 * Contributes platform saga error codes (execution and coordination errors).
 *
 * @since 2.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SagaErrorCodeContributor implements ErrorCodeContributor { // TODO: Move to platform

    @Override
    public Collection<? extends CodedMessage<?>> contributedCodes() {
        return List.of(SagaErrors.values());
    }
}
