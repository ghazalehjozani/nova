package ir.dotin.loan.trade.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ir.dotin.platform.commons.core.feature.FeatureConfig;

@Configuration
public class DomainConfig {

    @Bean
    public FeatureConfig featureConfig() {
        return new FeatureConfig(Map.of("test", true));
    }
}
