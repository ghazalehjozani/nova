package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("kafka-fcb")
@EnableConfigurationProperties(FcbHealthProperties.class)
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
}
