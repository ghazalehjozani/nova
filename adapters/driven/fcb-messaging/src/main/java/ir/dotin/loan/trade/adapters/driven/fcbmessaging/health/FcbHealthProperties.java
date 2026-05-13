package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.time.Duration;
import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = FcbHealthProperties.PREFIX)
public record FcbHealthProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("5s") @NotNull Duration probeInterval,
        @DefaultValue("1s") @NotNull Duration recoveryProbeInterval,
        @NotNull Duration probeTimeout,
        @DefaultValue("500ms") @NotNull Duration degradedLatencyThreshold,
        @Min(2) @Max(64) int probeWindowSize,
        @DefaultValue("5") @Min(1) int failuresInWindowToOpen,
        @DefaultValue("7") @Min(1) int successesInWindowToClose,
        @DefaultValue("15s") @NotNull Duration initialDelay,
        @DefaultValue("true") boolean failOpenOnUnknown,
        @DefaultValue("heartbeat") String heartbeatOperationName,
        @DefaultValue("NOVA") String producerCode,
        List<String> legacyConsumerGroupIds) {

    public static final String PREFIX = "nova.fcb.kafka.health";
}
