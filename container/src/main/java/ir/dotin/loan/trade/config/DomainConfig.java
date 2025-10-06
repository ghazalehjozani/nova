package ir.dotin.loan.trade.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import ir.dotin.platform.commons.core.feature.FeatureConfig;
import ir.dotin.platform.commons.domain.annotation.DomainComponent;
import ir.dotin.platform.commons.domain.annotation.DomainFactory;
import ir.dotin.platform.commons.domain.annotation.DomainService;

@Configuration
@ComponentScan(
        basePackages = "ir.dotin.loan",
        includeFilters =
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = {DomainComponent.class, DomainService.class, DomainFactory.class}))
public class DomainConfig {

    @Bean
    public FeatureConfig featureConfig() {
        return new FeatureConfig(Map.of("test", true));
    }
}
