package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.kafka.KafkaException;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbClientException;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception.FcbServerException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("kafka-fcb")
@RequiredArgsConstructor
public class FcbResilienceConfig {

    public static final String FCB_KAFKA_RETRY_TEMPLATE = "fcbKafkaRetryTemplate";
    public static final String FCB_KAFKA_CB = "fcb-kafka-cb";

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Bean(FCB_KAFKA_RETRY_TEMPLATE)
    public RetryTemplate fcbKafkaRetryTemplate() {
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(3L)
                .delay(Duration.ofMillis(500))
                .multiplier(2.0)
                .maxDelay(Duration.ofSeconds(10))
                .includes(FcbServerException.class)
                .includes(KafkaException.class)
                .includes(TimeoutException.class)
                .build();
        return new RetryTemplate(retryPolicy);
    }

    @PostConstruct
    public void configureCircuitBreaker() {
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(60)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .recordExceptions(FcbServerException.class, KafkaException.class, TimeoutException.class)
                .ignoreExceptions(FcbClientException.class)
                .build();

        circuitBreakerRegistry.addConfiguration(FCB_KAFKA_CB, cbConfig);
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(FCB_KAFKA_CB, cbConfig);
        cb.getEventPublisher()
                .onStateTransition(ev -> log.info(
                        "FCB Kafka Circuit Breaker state transition: {} -> {}",
                        ev.getStateTransition().getFromState(),
                        ev.getStateTransition().getToState()));
    }
}
