package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
@Profile("kafka-fcb")
@EnableConfigurationProperties({FcbHealthProperties.class, HealthActorProperties.class})
@ConditionalOnProperty(
        prefix = FcbHealthProperties.PREFIX,
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class FcbHealthAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public Clock fcbHealthClock() {
        return Clock.systemUTC();
    }

    @Bean
    public Gauge fcbHealthyPartitionsGauge(MeterRegistry registry, FcbPartitionHealthRegistry partitions) {
        return Gauge.builder("fcb.kafka.partitions.healthy", partitions, FcbPartitionHealthRegistry::healthyCount)
                .description("Number of healthy FCB Kafka partitions")
                .register(registry);
    }

    @Bean
    public Gauge fcbKnownPartitionsGauge(MeterRegistry registry, FcbPartitionHealthRegistry partitions) {
        return Gauge.builder("fcb.kafka.partitions.known", partitions, FcbPartitionHealthRegistry::totalKnown)
                .description("Total partitions tracked by FCB health probe")
                .register(registry);
    }
}
