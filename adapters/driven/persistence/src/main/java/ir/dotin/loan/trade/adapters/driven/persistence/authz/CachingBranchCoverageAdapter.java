package ir.dotin.loan.trade.adapters.driven.persistence.authz;

import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.BranchCoveragePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;

@Component
public class CachingBranchCoverageAdapter implements BranchCoveragePort {

    private static final Logger log = LoggerFactory.getLogger(CachingBranchCoverageAdapter.class);
    private static final String KEY_PREFIX = "nova:authz:branch-coverage:v1:";
    private static final String SEPARATOR = ",";

    private final LoanServicePort loanServicePort;
    private final StringRedisTemplate redisTemplate;
    private final BranchCoverageCacheProperties properties;

    public CachingBranchCoverageAdapter(
            LoanServicePort loanServicePort,
            StringRedisTemplate redisTemplate,
            BranchCoverageCacheProperties properties) {
        this.loanServicePort = loanServicePort;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public Result<List<BranchCode>> coveredBranches(BranchCode branchCode) {
        if (!properties.isEnabled()) {
            return loanServicePort.loadCoveredBranches(branchCode);
        }
        String key = KEY_PREFIX + branchCode.value();
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
            return decode(value);
        } catch (Exception e) {
            log.warn("Branch-coverage cache read failed, falling back to source [key={}]: {}", key, e.getMessage());
            return null;
        }
    }

    private void writeCache(String key, List<BranchCode> covered) {
        try {
            redisTemplate.opsForValue().set(key, encode(covered), properties.getTtl());
        } catch (Exception e) {
            log.warn("Branch-coverage cache write failed [key={}]: {}", key, e.getMessage());
        }
    }

    private static String encode(List<BranchCode> covered) {
        return String.join(
                SEPARATOR, covered.stream().map(BranchCode::value).toList());
    }

    private static @Nullable List<BranchCode> decode(String value) {
        if (value.isEmpty()) {
            return List.of();
        }
        try {
            return Arrays.stream(value.split(SEPARATOR))
                    .map(code -> BranchCode.of(code).unwrap())
                    .toList();
        } catch (Exception e) {
            log.warn("Branch-coverage cache entry corrupt, falling back to source: {}", e.getMessage());
            return null;
        }
    }
}
