package ir.dotin.loan.trade.adapters.driven.persistence.authz;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class CachingBranchCoverageAdapterTest {

    private static final String KEY = "nova:authz:branch-coverage:v1:001";

    @Mock
    private LoanServicePort loanServicePort;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private BranchCoverageCacheProperties properties;

    private CachingBranchCoverageAdapter adapter;

    private final BranchCode branch = BranchCode.of("001").unwrap();

    @BeforeEach
    void setUp() {
        properties = new BranchCoverageCacheProperties();
        adapter = new CachingBranchCoverageAdapter(loanServicePort, redisTemplate, properties);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void missLoadsFromSourceAndWritesCacheWithTtl() {
        when(valueOperations.get(KEY)).thenReturn(null);
        when(loanServicePort.loadCoveredBranches(branch))
                .thenReturn(Result.success(List.of(
                        BranchCode.of("123").unwrap(), BranchCode.of("456").unwrap())));

        Result<List<BranchCode>> result = adapter.coveredBranches(branch);

        assertThat(result.unwrap()).hasSize(2);
        verify(valueOperations).set(KEY, "123,456", Duration.ofMinutes(10));
    }

    @Test
    void hitSkipsSource() {
        when(valueOperations.get(KEY)).thenReturn("123,456");

        Result<List<BranchCode>> result = adapter.coveredBranches(branch);

        assertThat(result.unwrap())
                .containsExactly(
                        BranchCode.of("123").unwrap(), BranchCode.of("456").unwrap());
        verifyNoInteractions(loanServicePort);
    }

    @Test
    void cachedEmptyListIsServedWithoutSourceCall() {
        when(valueOperations.get(KEY)).thenReturn("");

        Result<List<BranchCode>> result = adapter.coveredBranches(branch);

        assertThat(result.unwrap()).isEmpty();
        verifyNoInteractions(loanServicePort);
    }

    @Test
    void disabledCacheIsPureDelegation() {
        properties.setEnabled(false);
        when(loanServicePort.loadCoveredBranches(branch)).thenReturn(Result.success(List.of()));

        Result<List<BranchCode>> result = adapter.coveredBranches(branch);

        assertThat(result.isSuccess()).isTrue();
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void redisReadFailureFallsBackToSource() {
        when(valueOperations.get(KEY)).thenThrow(new IllegalStateException("redis down"));
        when(loanServicePort.loadCoveredBranches(branch)).thenReturn(Result.success(List.of()));

        Result<List<BranchCode>> result = adapter.coveredBranches(branch);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void redisWriteFailureStillReturnsSourceResult() {
        when(valueOperations.get(KEY)).thenReturn(null);
        when(loanServicePort.loadCoveredBranches(branch))
                .thenReturn(Result.success(List.of(BranchCode.of("123").unwrap())));
        org.mockito.Mockito.doThrow(new IllegalStateException("redis down"))
                .when(valueOperations)
                .set(anyString(), anyString(), any(Duration.class));

        Result<List<BranchCode>> result = adapter.coveredBranches(branch);

        assertThat(result.unwrap()).hasSize(1);
    }

    @Test
    void sourceFailureIsNotCached() {
        when(valueOperations.get(KEY)).thenReturn(null);
        when(loanServicePort.loadCoveredBranches(branch))
                .thenReturn(Result.failure(CoreBankingErrors.BRANCH_NOT_COVERED, "001"));

        Result<List<BranchCode>> result = adapter.coveredBranches(branch);

        assertThat(result.isFailure()).isTrue();
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }
}
