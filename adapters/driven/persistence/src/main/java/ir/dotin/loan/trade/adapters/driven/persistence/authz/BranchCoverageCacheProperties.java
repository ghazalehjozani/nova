package ir.dotin.loan.trade.adapters.driven.persistence.authz;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "nova.branch-coverage.cache")
public class BranchCoverageCacheProperties {

    private boolean enabled = true;

    private Duration ttl = Duration.ofMinutes(10);
}
