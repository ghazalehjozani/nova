package ir.dotin.loan.trade.adapters.driven.persistence.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Operational endpoint to clear the FCB-static caches (Caffeine L1 + Redis L2) on demand.
 *
 * <p>Triggered by SREs after an FCB schema change or feature-flag flip whose cached results should no longer be served.
 * Clears every cache name declared in {@link LoanStaticCacheConfig#STATIC_CACHE_NAMES} from BOTH managers — never
 * clears unrelated cache namespaces (oauth2-tokens, jwt-jwks, delegation:accessTokenCache).
 *
 * <p>Exposure of this endpoint is controlled via {@code management.endpoints.web.exposure.include} in
 * {@code nova-config/kv/core/loan/nova/application/management.yml}.
 */
@Slf4j
@Component
@Endpoint(id = "loanCacheRefresh")
public class LoanStaticCacheRefreshEndpoint {

    private final LoanStaticCacheConfig cacheConfig;
    private final CacheManager redisManager;

    public LoanStaticCacheRefreshEndpoint(LoanStaticCacheConfig cacheConfig, CacheManager cacheManager) {
        this.cacheConfig = cacheConfig;
        this.redisManager = cacheManager;
    }

    @WriteOperation
    public Map<String, List<String>> refresh() {
        for (String name : LoanStaticCacheConfig.STATIC_CACHE_NAMES) {
            cacheConfig.clearLocal(name);
            Cache redis = redisManager.getCache(name);
            if (redis != null) {
                redis.clear();
            }
        }
        log.info("Loan static caches evicted: {}", LoanStaticCacheConfig.STATIC_CACHE_NAMES);
        Map<String, List<String>> result = new LinkedHashMap<>();
        result.put("evicted", LoanStaticCacheConfig.STATIC_CACHE_NAMES);
        return result;
    }
}
