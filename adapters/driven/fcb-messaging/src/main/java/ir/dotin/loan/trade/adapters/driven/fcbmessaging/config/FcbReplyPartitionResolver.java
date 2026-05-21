package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Derives a stable partition index for this pod on a shared reply topic.
 *
 * <p>Priority for the source identifier:
 *
 * <ol>
 *   <li>Configured {@code platform.messaging.kafka.instance-id}
 *   <li>{@code KUBERNETES_POD_NAME} env (K8s StatefulSet)
 *   <li>{@code HOSTNAME} env
 *   <li>Random fallback (dev only)
 * </ol>
 *
 * <p>If the identifier ends with a numeric ordinal (e.g. {@code nova-svc-3}), that ordinal is used directly modulo the
 * partition count — stable across restarts. Otherwise the identifier's {@code hashCode()} is used. Two pods can collide
 * on the same partition when pod count exceeds partition count; that degrades to current shared-topic behaviour without
 * breaking correctness (correlation header still filters).
 */
public final class FcbReplyPartitionResolver {

    private static final Logger LOG = LoggerFactory.getLogger(FcbReplyPartitionResolver.class);
    private static final Pattern TRAILING_ORDINAL = Pattern.compile(".*-(\\d+)$");

    private FcbReplyPartitionResolver() {}

    public static int resolve(String configuredInstanceId, int partitionCount) {
        if (partitionCount <= 0) {
            throw new IllegalArgumentException("partitionCount must be > 0, was: " + partitionCount);
        }
        String source = pickSource(configuredInstanceId);
        Integer ordinal = parseTrailingOrdinal(source);
        int partition;
        if (ordinal != null) {
            partition = Math.floorMod(ordinal, partitionCount);
            LOG.info(
                    "FCB-REPLY-PARTITION: assigned partition={} via ordinal source='{}' partitions={}",
                    partition,
                    source,
                    partitionCount);
        } else {
            partition = Math.floorMod(source.hashCode(), partitionCount);
            LOG.info(
                    "FCB-REPLY-PARTITION: assigned partition={} via hash source='{}' partitions={}",
                    partition,
                    source,
                    partitionCount);
        }
        // Bounds invariant: floorMod(x, n) is always in [0, n) for n > 0. Assert it at startup so any future change to
        // the derivation that breaks the invariant fails fast at bean creation instead of producing an out-of-range
        // KafkaHeaders.REPLY_PARTITION the broker would reject at send time.
        if (partition < 0 || partition >= partitionCount) {
            throw new IllegalStateException("FCB-REPLY-PARTITION: resolved partition=" + partition + " out of range [0,"
                    + partitionCount + ") for source='" + source + "'");
        }
        return partition;
    }

    private static String pickSource(String configuredInstanceId) {
        if (configuredInstanceId != null && !configuredInstanceId.isBlank()) {
            return configuredInstanceId;
        }
        String pod = System.getenv("KUBERNETES_POD_NAME");
        if (pod != null && !pod.isBlank()) {
            return pod;
        }
        String host = System.getenv("HOSTNAME");
        if (host != null && !host.isBlank()) {
            return host;
        }
        LOG.warn(
                "FCB-REPLY-PARTITION: no KUBERNETES_POD_NAME/HOSTNAME/instance-id available; using random fallback (dev only)");
        return "fallback-" + System.nanoTime();
    }

    private static Integer parseTrailingOrdinal(String source) {
        Matcher m = TRAILING_ORDINAL.matcher(source);
        if (!m.matches()) {
            return null;
        }
        try {
            return Integer.parseInt(m.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
