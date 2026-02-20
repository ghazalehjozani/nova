package ir.dotin.loan.trade.adapters.driven.persistence.guard;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import ir.dotin.loan.trade.core.application.ports.outbound.client.ConcurrentApplicationOperationException;
import ir.dotin.loan.trade.core.application.ports.outbound.client.LegacyOperationGuardPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLegacyOperationGuard implements LegacyOperationGuardPort {

    private static final String KEY_PREFIX = "fcb-op-lock:";

    private final StringRedisTemplate redisTemplate;

    @Value("${fcb.guard.ttl-seconds:60}")
    private long ttlSeconds;

    @Override
    public void acquire(String applicationId) {
        String key = KEY_PREFIX + applicationId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "locked", Duration.ofSeconds(ttlSeconds));

        if (!Boolean.TRUE.equals(acquired)) {
            log.warn("Failed to acquire lock for application: {}", applicationId);
            throw new ConcurrentApplicationOperationException(applicationId);
        }

        log.debug("Lock acquired for application: {}", applicationId);
    }

    @Override
    public void release(String applicationId) {
        String key = KEY_PREFIX + applicationId;
        Boolean deleted = redisTemplate.delete(key);
        log.debug("Lock released for application: {} (deleted={})", applicationId, deleted);
    }
}
