package ir.dotin.loan.trade.core.application.service.configuration;

import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "trade.features")
public class TradeFeatureProperties {

    @Nullable
    private Map<String, Boolean> features;
}
