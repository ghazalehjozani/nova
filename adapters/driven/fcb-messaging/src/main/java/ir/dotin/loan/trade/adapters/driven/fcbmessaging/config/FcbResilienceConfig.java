package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.kafka.KafkaException;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbServerException;

/**
 * Retry template for transient Kafka failures.
 *
 * <p>The previous {@code Resilience4j} circuit breaker has been removed and replaced by
 * {@link ir.dotin.loan.trade.adapters.driven.fcbmessaging.health.FcbHealthGate}, which relies on an active heartbeat
 * probe rather than consuming user requests as probes. See the package-level README for rationale.
 *
 * <p>Retry is now only applied to genuinely transient conditions and is intentionally bounded — in a financial system
 * you do not want to burn seconds retrying an already-broken downstream. The gate catches sustained outages almost
 * immediately after the probe classifies FCB as DOWN.
 */
@Configuration
@Profile("kafka-fcb")
public class FcbResilienceConfig {

    public static final String FCB_KAFKA_RETRY_TEMPLATE = "fcbKafkaRetryTemplate";

    @Bean(FCB_KAFKA_RETRY_TEMPLATE)
    public RetryTemplate fcbKafkaRetryTemplate() {
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(2L)
                .delay(Duration.ofMillis(250))
                .multiplier(2.0)
                .maxDelay(Duration.ofSeconds(2))
                .includes(FcbServerException.class)
                .includes(KafkaException.class)
                .includes(TimeoutException.class)
                .build();
        return new RetryTemplate(retryPolicy);
    }
}
