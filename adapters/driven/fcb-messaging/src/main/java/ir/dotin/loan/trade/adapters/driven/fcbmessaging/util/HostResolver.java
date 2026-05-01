package ir.dotin.loan.trade.adapters.driven.fcbmessaging.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostResolver {

    private static final String UNKNOWN = "unknown";
    private static volatile String cachedHost;

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
        }
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            int at = name.indexOf('@');
            if (at >= 0 && at + 1 < name.length()) {
                return name.substring(at + 1);
            }
        } catch (Throwable ignored) {
        }
        return UNKNOWN;
    }
}
