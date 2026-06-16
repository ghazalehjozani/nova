package ir.dotin.loan.trade.core.application.service.configuration;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import ir.dotin.platform.pangaea.commons.core.feature.FeatureConfig;

@Data
@ConfigurationProperties(prefix = "trade.features")
public class TradeFeatureProperties {

    @Nullable
    private Map<String, Boolean> features;

}
