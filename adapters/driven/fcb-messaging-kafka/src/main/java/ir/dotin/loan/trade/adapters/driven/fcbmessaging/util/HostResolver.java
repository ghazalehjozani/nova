package ir.dotin.loan.trade.adapters.driven.fcbmessaging.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jspecify.annotations.Nullable;

public class HostResolver {

    private static final String UNKNOWN = "unknown";
    // lazily computed cache; null until first resolveHostName() call (double-checked locking)
    private static volatile @Nullable String cachedHost;

    private HostResolver() {}

    public static String resolveHostName() {
        String c = cachedHost;
        if (c != null) {
            return c;
        }
        synchronized (HostResolver.class) {
            if (cachedHost != null) {
                return cachedHost;
            }
            cachedHost = compute();
            return cachedHost;
        }
    }

    private static String compute() {
        String env = System.getenv("HOSTNAME");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        env = System.getenv("COMPUTERNAME");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        try {
            String h = InetAddress.getLocalHost().getHostName();
            if (h != null && !h.isBlank()) {
                return h.trim();
            }
        } catch (UnknownHostException ignored) {
            // why: local-hostname lookup failed; fall through to the JVM runtime-name fallback below
        }
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            int at = name.indexOf('@');
            if (at >= 0 && at + 1 < name.length()) {
                return name.substring(at + 1);
            }
        } catch (Throwable ignored) {
            // why: runtime MXBean name unavailable/malformed; fall through to the UNKNOWN sentinel below
        }
        return UNKNOWN;
    }
}
