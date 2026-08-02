package ir.dotin.loan.trade.adapters.driven.persistence.config;

import org.jspecify.annotations.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.serializer.SerializationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResilientCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, @NonNull Object key) {
        if (causedBySerializationFailure(exception)) {
            log.warn("Cache GET found a corrupt value for cache='{}'; deleting that entry", cache.getName());
            try {
                cache.evict(key);
            } catch (RuntimeException evictFailure) {
                log.error("Failed to evict corrupt cache entry for cache='{}'", cache.getName(), evictFailure);
            }
        } else {
            log.warn(
                    "Cache GET failed for cache='{}'; preserving the entry because the failure may be transient",
                    cache.getName(),
                    exception);
        }
    }

    @Override
    public void handleCachePutError(
            @NonNull RuntimeException exception,
            Cache cache,
            @NonNull Object key,
            @org.jspecify.annotations.Nullable Object value) {
        log.error("Cache PUT error for cache='{}'", cache.getName(), exception);
    }

    @Override
    public void handleCacheEvictError(@NonNull RuntimeException exception, Cache cache, @NonNull Object key) {
        log.error("Cache EVICT error for cache='{}'", cache.getName(), exception);
    }

    @Override
    public void handleCacheClearError(@NonNull RuntimeException exception, Cache cache) {
        log.error("Cache CLEAR error for cache='{}'", cache.getName(), exception);
    }

    private boolean causedBySerializationFailure(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof SerializationException
                    || current.getClass().getName().startsWith("tools.jackson.")
                    || current.getClass().getName().startsWith("com.fasterxml.jackson.")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
