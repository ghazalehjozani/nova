package ir.dotin.loan.trade.adapters.driving.messaging.activemq.config;

/**
 * Trade-loan-specific JMS configuration.
 *
 * <p>The base JMS infrastructure (listener container factory, JmsTemplate, connection pooling) is now provided by
 * {@code platform-spring-boot-starter-messaging-activemq}. This class only declares trade-loan-specific constants and
 * overrides.
 */
public final class ActiveMqJmsConfig {

    private ActiveMqJmsConfig() {}

    /** Full lifecycle request queue. */
    public static final String FULL_LIFECYCLE_QUEUE = "corridor.core.loan.nova.full-lifecycle.request.queue.v1";
}
