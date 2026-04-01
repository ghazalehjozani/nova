package ir.dotin.loan.trade.error;

import ir.dotin.platform.commons.core.error.CodedMessage;
import ir.dotin.platform.protocol.api.error.ErrorCodeContributor;
import ir.dotin.platform.saga.api.error.SagaErrors;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

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
