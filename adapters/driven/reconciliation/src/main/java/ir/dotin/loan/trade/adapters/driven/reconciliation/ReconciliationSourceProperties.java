package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "reconciliation")
public class ReconciliationSourceProperties {

    /** Upper bound on the page size the facility reconciliation source requests from the read port. */
    private int sourceBatchSize = 200;

    /** Detection-settling floor: a facility is not emitted for probing until quiescent at least this long. */
    private Duration detectionSettle = Duration.ofMinutes(3);
}
