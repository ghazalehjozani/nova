package ir.dotin.loan.trade.adapters.driven.persistence.config;

import java.time.Duration;
import java.util.List;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Configuration;

/**
 * Routes only the five FCB static caches to a bounded local Caffeine manager. Every other cache is resolved from Spring
 * Boot's RedisCacheManager, so this configuration does not cause Redis cache auto-configuration to back off.
 */
@Configuration
public class LoanStaticCacheConfig implements CachingConfigurer {

    static final List<String> STATIC_CACHE_NAMES = List.of(
            "fcb.economical-sector",
            "fcb.economical-sector-by-code",
            "fcb.sector-for-loan-type",
            "fcb.resource",
            "fcb.reason-type");

    private final CaffeineCacheManager localManager;
    private final ObjectProvider<CacheManager> bootCacheManager;

    public LoanStaticCacheConfig(ObjectProvider<CacheManager> bootCacheManager) {
        this.bootCacheManager = bootCacheManager;
        this.localManager = new CaffeineCacheManager(STATIC_CACHE_NAMES.toArray(String[]::new));
        this.localManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofSeconds(30))
                .recordStats());
    }

    @Override
    public CacheResolver cacheResolver() {
        return new RoutingCacheResolver();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new ResilientCacheErrorHandler();
    }

    void clearLocal(String name) {
        Cache cache = localManager.getCache(name);
        if (cache != null) {
            cache.clear();
        }
    }

    private final class RoutingCacheResolver implements CacheResolver {
        @Override
        public java.util.Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
            CacheManager redis = bootCacheManager.getObject();
            return context.getOperation().getCacheNames().stream()
                    .map(name -> STATIC_CACHE_NAMES.contains(name) ? localManager.getCache(name) : redis.getCache(name))
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
    }
}
