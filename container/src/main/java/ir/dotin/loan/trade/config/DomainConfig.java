package ir.dotin.loan.trade.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import ir.dotin.platform.commons.core.feature.FeatureConfig;
import ir.dotin.platform.commons.domain.annotation.DomainComponent;

@Configuration
@ComponentScan(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = DomainComponent.class))
public class DomainConfig {

    @Bean
    public FeatureConfig featureConfig() {
        return new FeatureConfig(Map.of("test", true));
    }
}
