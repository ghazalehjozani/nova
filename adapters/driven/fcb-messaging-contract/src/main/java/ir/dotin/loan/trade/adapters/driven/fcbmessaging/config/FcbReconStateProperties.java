package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "reconciliation")
public class FcbReconStateProperties {

    /** Per-call timeout for the recon-state / re-emit FCB request-reply. */
    private Duration reconStateTimeout = Duration.ofSeconds(10);
}
