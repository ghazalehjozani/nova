package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.common.GroupState;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("legacyConsumerGroupHealth")
public class LegacyConsumerGroupHealthIndicator extends AbstractHealthIndicator {

    private static final Duration REFRESH_INTERVAL = Duration.ofSeconds(30);
    private static final Duration ADMIN_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration STALE_THRESHOLD = Duration.ofMinutes(2);

    private final AdminClient adminClient;
    private final List<String> consumerGroupIds;
    private final AtomicReference<CachedResult> cache = new AtomicReference<>(CachedResult.unknown());
    private ScheduledExecutorService scheduler;

    public LegacyConsumerGroupHealthIndicator(KafkaAdmin kafkaAdmin, FcbHealthProperties properties) {
        this.adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
        this.consumerGroupIds = properties.legacyConsumerGroupIds();
    }

    @PostConstruct
    public void start() {
        if (consumerGroupIds == null || consumerGroupIds.isEmpty()) return;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "legacy-cg-health-refresh");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(this::refresh, 0, REFRESH_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void close() {
        if (scheduler != null) scheduler.shutdownNow();
        adminClient.close(Duration.ofSeconds(2));
    }

    @Override
    protected void doHealthCheck(Health.@NonNull Builder builder) {
        CachedResult result = cache.get();

        if (result.staleSince(STALE_THRESHOLD)) {
            builder.status(Status.UNKNOWN)
                    .withDetail("reason", "consumer group health data is stale")
                    .withDetail("lastCheck", result.timestamp.toString());
            return;
        }

        switch (result.status) {
            case UP -> builder.up();
            case DOWN -> builder.down();
            case DEGRADED -> builder.status("DEGRADED");
            case UNKNOWN -> builder.status(Status.UNKNOWN);
        }
        builder.withDetail("lastCheck", result.timestamp.toString()).withDetails(result.details);
    }

    private void refresh() {
        try {
            var describe = adminClient.describeConsumerGroups(consumerGroupIds);
            int totalActive = 0;
            int stableCount = 0;
            int deadCount = 0;
            var details = new java.util.LinkedHashMap<String, Object>();

            for (String groupId : consumerGroupIds) {
                try {
                    ConsumerGroupDescription desc = describe.describedGroups()
                            .get(groupId)
                            .get(ADMIN_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
                    GroupState state = GroupState.parse(desc.groupState().toString());
                    int members = desc.members().size();
                    totalActive += members;
                    if (state == GroupState.STABLE) stableCount++;
                    if (state == GroupState.DEAD || state == GroupState.EMPTY) deadCount++;
                    details.put(groupId + ".state", state.name());
                    details.put(groupId + ".members", members);
                } catch (Exception perGroupEx) {
                    details.put(groupId + ".error", perGroupEx.getClass().getSimpleName());
                    log.debug("Per-group describe failed groupId={}", groupId, perGroupEx);
                }
            }

            CachedResult.Status status;
            if (deadCount == consumerGroupIds.size()) status = CachedResult.Status.DOWN;
            else if (stableCount == consumerGroupIds.size()) status = CachedResult.Status.UP;
            else status = CachedResult.Status.DEGRADED;

            details.put("totalActiveMembers", totalActive);
            cache.set(new CachedResult(status, details, Instant.now()));
        } catch (Throwable t) {
            log.warn("Background refresh of consumer-group health failed: {}", t.toString());
            CachedResult prev = cache.get();
            if (prev.status == CachedResult.Status.UNKNOWN) {
                cache.set(new CachedResult(
                        CachedResult.Status.UNKNOWN,
                        java.util.Map.of("error", t.getClass().getSimpleName()),
                        Instant.now()));
            }
        }
    }

    private record CachedResult(Status status, java.util.Map<String, Object> details, Instant timestamp) {
        enum Status {
            UP,
            DOWN,
            DEGRADED,
            UNKNOWN
        }

        static CachedResult unknown() {
            return new CachedResult(Status.UNKNOWN, java.util.Map.of(), Instant.EPOCH);
        }

        boolean staleSince(Duration threshold) {
            return Duration.between(timestamp, Instant.now()).compareTo(threshold) > 0;
        }
    }
}
