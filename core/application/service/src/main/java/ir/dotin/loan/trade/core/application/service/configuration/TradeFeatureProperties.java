package ir.dotin.loan.trade.core.application.service.configuration;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.context.properties.ConfigurationProperties;

import ir.dotin.platform.pangaea.commons.core.feature.FeatureConfig;

@ConfigurationProperties(prefix = "trade.features")
public record TradeFeatureProperties(Map<String, Boolean> features) {

    public TradeFeatureProperties {
        features = features == null ? createDefaultMappings() : ImmutableMap.copyOf(features);
    }

    private static Map<String, Boolean> createDefaultMappings() {
        return ImmutableMap.of("test", true);
    }

    public FeatureConfig featureConfig() {
        return new FeatureConfig(this.features);
    }
}
