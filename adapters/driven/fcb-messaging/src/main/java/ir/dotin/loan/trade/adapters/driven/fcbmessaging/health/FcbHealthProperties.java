package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.time.Duration;
import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = FcbHealthProperties.PREFIX)
public class FcbHealthProperties {

    public static final String PREFIX = "nova.fcb.kafka.health";

    private boolean enabled = true;

    @NotNull
    private Duration probeInterval = Duration.ofSeconds(5);

    @NotNull
    private Duration recoveryProbeInterval = Duration.ofSeconds(1);

    @NotNull
    private Duration probeTimeout;

    @NotNull
    private Duration degradedLatencyThreshold = Duration.ofMillis(500);

    @Min(2)
    @Max(64)
    private int probeWindowSize = 10;

    @Min(1)
    private int failuresInWindowToOpen = 5;

    @Min(1)
    private int successesInWindowToClose = 7;

    @NotNull
    private Duration initialDelay = Duration.ofSeconds(15);

    private boolean failOpenOnUnknown = true;

    private String heartbeatOperationName = "heartbeat";

    private String producerCode = "NOVA";

    private List<String> legacyConsumerGroupIds = List.of("core.loan.nova.fcb-integration.v1");
}
