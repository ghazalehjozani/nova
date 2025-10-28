package ir.dotin.loan.trade.adapters.driven.fcbclient.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "fcb.health")
public class FcbHealthProperties {

    /** Enable or disable FCB health checks */
    private boolean enabled = true;

    /**
     * The FCB usecase to execute for health verification. This should be a lightweight operation that verifies
     * connectivity.
     */
    @NotBlank
    private String testUsecase = "system-health-check";

    /** Timeout for health check requests in seconds */
    @Min(1)
    private int timeoutSeconds = 10;

    /**
     * Duration to cache health check results in seconds. Caching prevents overwhelming the FCB system with frequent
     * health checks.
     */
    @Min(5)
    private int cacheDurationSeconds = 30;

    /** Number of consecutive failures before marking the service as DOWN */
    @Min(1)
    private int failureThreshold = 3;

    /** Show detailed error information in health endpoint responses */
    private boolean showDetails = true;

    /** Enable periodic health check metrics collection */
    private boolean metricsEnabled = true;
}
