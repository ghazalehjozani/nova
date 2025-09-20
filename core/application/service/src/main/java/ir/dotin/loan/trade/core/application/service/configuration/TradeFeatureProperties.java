package ir.dotin.loan.trade.core.application.service.configuration;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import ir.dotin.platform.commons.core.feature.FeatureConfig;

@ConfigurationProperties(prefix = "trade.features")
public record TradeFeatureProperties(Map<String, Boolean> features) {

    public TradeFeatureProperties {
        features = features == null ? createDefaultMappings() : Map.copyOf(features);
    }

    private static Map<String, Boolean> createDefaultMappings() {
        return Map.of("test", true);
    }

    public FeatureConfig featureConfig() {
        return new FeatureConfig(this.features);
    }
}
