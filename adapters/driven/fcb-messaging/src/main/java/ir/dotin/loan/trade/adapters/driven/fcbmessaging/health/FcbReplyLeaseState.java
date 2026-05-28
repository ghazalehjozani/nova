package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import org.springframework.stereotype.Component;

/**
 * Shared flag flipped when this instance is shedding its FCB reply partition (graceful shutdown or a lost lease). While
 * degraded, {@link FcbKafkaReadinessIndicator} reports DOWN so the orchestrator / load balancer drains traffic away
 * before the reply consumer stops. Kept profile-agnostic (a plain POJO) so it is always injectable.
 */
@Component
public class FcbReplyLeaseState {

    private volatile boolean degraded = false;
    private volatile String reason = "";

    public void markDegraded(String reason) {
        this.reason = reason == null ? "" : reason;
        this.degraded = true;
    }

    public boolean isDegraded() {
        return degraded;
    }

    public String reason() {
        return reason;
    }
}
