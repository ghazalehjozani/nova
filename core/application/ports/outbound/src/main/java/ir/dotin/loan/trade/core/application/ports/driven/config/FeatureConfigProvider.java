package ir.dotin.loan.trade.core.application.ports.driven.config;

import java.util.Map;

import ir.dotin.platform.commons.core.feature.FeatureConfig;

public interface FeatureConfigProvider {
    FeatureConfig getFeatureConfig();

    boolean isFeatureEnabled(String feature);

    Map<String, Boolean> getAllFeatures();
}
