package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.time.Duration;
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
        @DefaultValue("2s") @NotNull Duration probeTimeout,
        @DefaultValue("500ms") @NotNull Duration degradedLatencyThreshold,
        @DefaultValue("3") @Min(1) int consecutiveFailuresToOpen,
        @DefaultValue("3") @Min(1) int consecutiveSuccessesToClose,
        @DefaultValue("15s") @NotNull Duration initialDelay,
        @DefaultValue("true") boolean failOpenOnUnknown,
        @DefaultValue("heartbeat") String heartbeatOperationName,
        @DefaultValue("NOVA") String producerCode) {

    public static final String PREFIX = "nova.fcb.kafka.health";
}
