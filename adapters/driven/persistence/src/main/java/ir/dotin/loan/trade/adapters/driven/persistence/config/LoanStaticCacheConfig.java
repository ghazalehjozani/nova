package ir.dotin.loan.trade.adapters.driven.persistence.config;

import java.time.Duration;
import java.util.List;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Two-tier cache manager for FCB static reads (economic-sector, sector-for-loan-type, resource, reason-type).
 *
 * <p>L1 Caffeine (30s TTL, in-process) is resolved first by Spring for the FCB cache names registered in
 * {@link #STATIC_CACHE_NAMES}. Any other cache name (oauth2-tokens, jwt-jwks, delegation:accessTokenCache, …) falls
 * through to the existing Redis-backed manager.
 *
 * <p>Semantics are <i>parallel</i> not <i>tiered</i>: a Redis hit does not repopulate Caffeine. Acceptable for the
 * warm-state ≤7s p50 latency target — Redis on LAN is sub-millisecond.
 */
@Configuration
public class LoanStaticCacheConfig {

    static final List<String> STATIC_CACHE_NAMES = List.of(
            "fcb.economical-sector",
            "fcb.economical-sector-by-code",
            "fcb.sector-for-loan-type",
            "fcb.resource",
            "fcb.reason-type");

    @Bean
    public CaffeineCacheManager caffeineLoanCacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager(STATIC_CACHE_NAMES.toArray(String[]::new));
        mgr.setCaffeine(Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(Duration.ofSeconds(30)));
        return mgr;
    }

    @Bean
    @Primary
    public CacheManager loanCacheManager(
            CaffeineCacheManager caffeineLoanCacheManager,
            @Qualifier("redisLoanCacheManager") CacheManager redisLoanCacheManager) {
        CompositeCacheManager composite = new CompositeCacheManager(caffeineLoanCacheManager, redisLoanCacheManager);
        composite.setFallbackToNoOpCache(false);
        return composite;
    }
}
