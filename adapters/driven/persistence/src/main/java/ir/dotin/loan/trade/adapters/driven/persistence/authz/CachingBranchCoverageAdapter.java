package ir.dotin.loan.trade.adapters.driven.persistence.authz;

import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.cache.CacheKeyspace;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.BranchCoveragePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;

@Component
public class CachingBranchCoverageAdapter implements BranchCoveragePort {

    private static final Logger log = LoggerFactory.getLogger(CachingBranchCoverageAdapter.class);
    private static final String BASE_PREFIX = "nova:authz:branch-coverage:";
    private static final String SEPARATOR = ",";

    private final LoanServicePort loanServicePort;
    private final StringRedisTemplate redisTemplate;
    private final BranchCoverageCacheProperties properties;
    private final String keyPrefix;

    public CachingBranchCoverageAdapter(
            LoanServicePort loanServicePort,
            StringRedisTemplate redisTemplate,
            BranchCoverageCacheProperties properties,
            @Value("${nova.branch-coverage.cache.version}") String cacheKeyVersion) {
        this.loanServicePort = loanServicePort;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.keyPrefix = CacheKeyspace.versioned(BASE_PREFIX, cacheKeyVersion);
    }

    @Override
    public Result<List<BranchCode>> coveredBranches(BranchCode branchCode) {
        if (!properties.isEnabled()) {
            return loanServicePort.loadCoveredBranches(branchCode);
        }
        String key = keyPrefix + branchCode.value();
        List<BranchCode> cached = readCache(key);
        if (cached != null) {
            return Result.success(cached);
        }
        return loanServicePort.loadCoveredBranches(branchCode).onSuccess(covered -> writeCache(key, covered));
    }

    private @Nullable List<BranchCode> readCache(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            List<BranchCode> decoded = decode(value);
            if (decoded == null) {
                try {
                    redisTemplate.delete(key);
                } catch (RuntimeException deleteFailure) {
                    log.debug("Could not delete corrupt branch-coverage entry", deleteFailure);
                }
            }
            return decoded;
        } catch (Exception e) {
            log.warn("Branch-coverage cache read failed, falling back to source: {}", e.getMessage());
            return null;
        }
    }

    private void writeCache(String key, List<BranchCode> covered) {
        try {
            redisTemplate.opsForValue().set(key, encode(covered), properties.getTtl());
        } catch (Exception e) {
            log.warn("Branch-coverage cache write failed: {}", e.getMessage());
        }
    }

    private static String encode(List<BranchCode> covered) {
        return String.join(SEPARATOR, covered.stream().map(BranchCode::value).toList());
    }

    private static @Nullable List<BranchCode> decode(String value) {
        if (value.isEmpty()) {
            return List.of();
        }
        try {
            String[] codes = value.split(SEPARATOR, -1);
            if (Arrays.stream(codes).anyMatch(String::isBlank)) {
                return null;
            }
            return Arrays.stream(codes)
                    .map(code -> BranchCode.of(code).unwrap())
                    .toList();
        } catch (Exception e) {
            log.warn("Branch-coverage cache entry corrupt, falling back to source: {}", e.getMessage());
            return null;
        }
    }
}
