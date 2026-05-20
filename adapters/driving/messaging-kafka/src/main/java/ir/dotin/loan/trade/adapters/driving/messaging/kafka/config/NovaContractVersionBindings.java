package ir.dotin.loan.trade.adapters.driving.messaging.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ir.dotin.platform.messaging.kafka.spi.ContractVersionTopicBinding;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.contract.dto.LoanFacilityRestructuringMessage;

/**
 * Publishes one {@link ContractVersionTopicBinding} per Nova-subscribed Kafka topic.
 *
 * <p>Pangaea's contract-version startup probe and inbound filter are gated on at least one bean of type
 * {@link ContractVersionTopicBinding} being present (see {@code ContractVersionStartupGuard} /
 * {@code ContractVersionHealthIndicator} — both {@code @ConditionalOnBean}). Without these bindings the gate is
 * dormant.
 *
 * <p>Topic names are hard-coded to match the {@code @KafkaListener} annotations in {@code consumer/FcbEventConsumer}
 * and {@code consumer/FullLifecycleKafkaCommandConsumer}; any rename of those topics must be reflected here in the same
 * change set.
 *
 * <p>The {@code installment-operation} envelope topic carries seven polymorphic {@code *Message} subtypes (one per
 * {@code FcbEventOperationType}); all currently live on the v1 baseline. We bind the topic to one representative
 * {@code *Message} class — {@link LoanFacilityRestructuringMessage}. Switching to a per-subtype binding becomes
 * necessary the first time those subtypes diverge in version.
 */
@Configuration
public class NovaContractVersionBindings {

    private static final String FCB_EVENT_TOPIC = "corridor.core.loan.nova.installment-operation.request.queue.v1";
    private static final String FULL_LIFECYCLE_TOPIC = "corridor.core.loan.nova.full-lifecycle.request.queue.v1";

    @Bean
    ContractVersionTopicBinding fcbEventTopicBinding() {
        record Binding(String topic, Class<?> handlerClass) implements ContractVersionTopicBinding {}
        return new Binding(FCB_EVENT_TOPIC, LoanFacilityRestructuringMessage.class);
    }

    @Bean
    ContractVersionTopicBinding fullLifecycleTopicBinding() {
        record Binding(String topic, Class<?> handlerClass) implements ContractVersionTopicBinding {}
        return new Binding(FULL_LIFECYCLE_TOPIC, FullLoanFacilityLifecycleMessage.class);
    }
}
