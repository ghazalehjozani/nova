package ir.dotin.loan.trade.adapters.driven.persistence.config;

import org.jspecify.annotations.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResilientCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, @NonNull Object key) {
        log.warn(
                "Cache GET error for cache='{}', key='{}'. Evicting stale entry. Cause: {}",
                cache.getName(),
                key,
                exception.getMessage());
        try {
            cache.evict(key);
        } catch (Exception evictEx) {
            log.error("Failed to evict stale cache entry for key='{}'", key, evictEx);
        }
    }

    @Override
    public void handleCachePutError(
            @NonNull RuntimeException exception,
            Cache cache,
            @NonNull Object key,
            @org.jspecify.annotations.Nullable Object value) {
        log.error("Cache PUT error for cache='{}', key='{}'", cache.getName(), key, exception);
    }

    @Override
    public void handleCacheEvictError(@NonNull RuntimeException exception, Cache cache, @NonNull Object key) {
        log.error("Cache EVICT error for cache='{}', key='{}'", cache.getName(), key, exception);
    }

    @Override
    public void handleCacheClearError(@NonNull RuntimeException exception, Cache cache) {
        log.error("Cache CLEAR error for cache='{}'", cache.getName(), exception);
    }
}
