package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.common.PartitionInfo;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthyPartitionSelector {

    private final ReplyingKafkaTemplate<String, byte[], byte[]> template;
    private final ConcurrentHashMap<Integer, Long> unhealthyPartitions = new ConcurrentHashMap<>();
    private static final long PENALTY_MS = 60000;

    public int select(String topic) {
        List<PartitionInfo> partitions = template.partitionsFor(topic);
        if (partitions == null || partitions.isEmpty()) {
            return 0;
        }
        long now = System.currentTimeMillis();
        List<Integer> healthy = partitions.stream()
                .map(PartitionInfo::partition)
                .filter(p -> !unhealthyPartitions.containsKey(p) || (now - unhealthyPartitions.get(p)) > PENALTY_MS)
                .toList();

        if (healthy.isEmpty()) {
            return partitions
                    .get(ThreadLocalRandom.current().nextInt(partitions.size()))
                    .partition();
        }
        return healthy.get(ThreadLocalRandom.current().nextInt(healthy.size()));
    }

    public void markUnhealthy(int partition) {
        unhealthyPartitions.put(partition, System.currentTimeMillis());
    }

    public void markHealthy(int partition) {
        unhealthyPartitions.remove(partition);
    }
}
