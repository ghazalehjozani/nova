package ir.dotin.loan.trade.adapters.driven.fcbclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FcbMetricsConfiguration {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        log.info("Configuring timed aspect for FCB metrics");
        return new TimedAspect(registry);
    }
}
