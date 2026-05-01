package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

@Component
public class FcbRemoteHealthState {

    private static final int MAX_TRACKED_HOSTS = 64;
    private static final long STALE_THRESHOLD_MINUTES = 5;

    private final ConcurrentMap<String, FcbRemoteHealthSnapshot> byHost = new ConcurrentHashMap<>();

    public void updateForHost(String host, FcbRemoteHealthSnapshot snapshot) {
        if (host == null || host.isEmpty() || snapshot == null) {
            return;
        }
        if (byHost.size() >= MAX_TRACKED_HOSTS && !byHost.containsKey(host)) {
            evictStaleHosts();
            if (byHost.size() >= MAX_TRACKED_HOSTS) {
                return;
            }
        }
        byHost.put(host, snapshot);
    }

    public List<FcbRemoteHealthSnapshot> allSnapshots() {
        return List.copyOf(byHost.values());
    }

    public boolean hasAnyFreshSnapshot() {
        for (FcbRemoteHealthSnapshot s : byHost.values()) {
            if (s.isFresh()) {
                return true;
            }
        }
        return false;
    }

    public FcbRemoteHealthSnapshot snapshotForHost(String host) {
        if (host == null) {
            return FcbRemoteHealthSnapshot.empty();
        }
        FcbRemoteHealthSnapshot s = byHost.get(host);
        return s != null ? s : FcbRemoteHealthSnapshot.empty();
    }

    private void evictStaleHosts() {
        Instant threshold = Instant.now().minus(STALE_THRESHOLD_MINUTES, ChronoUnit.MINUTES);
        byHost.entrySet().removeIf(entry -> entry.getValue().observedAt().isBefore(threshold));
    }
}
