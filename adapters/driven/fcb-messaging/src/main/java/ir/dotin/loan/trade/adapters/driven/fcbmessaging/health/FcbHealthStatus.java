package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

public enum FcbHealthStatus {

    /** No probe has completed yet. Gate behaviour is governed by failOpenOnUnknown. */
    UNKNOWN,

    /** Last N probes succeeded within SLO. Traffic flows normally. */
    UP,

    /** Probes succeed but latency exceeds degradedLatencyThreshold. Traffic still flows, alert only. */
    DEGRADED,

    /** Consecutive probe failures exceeded threshold. Gate is CLOSED — user traffic fast-fails. */
    DOWN,

    /** Previously DOWN, probes are succeeding again but not yet enough to declare UP. Traffic flows. */
    RECOVERING;

    public boolean isAvailable() {
        return this == UP || this == DEGRADED || this == RECOVERING;
    }
}
