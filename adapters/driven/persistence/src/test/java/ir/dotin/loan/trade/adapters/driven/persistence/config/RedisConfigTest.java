package ir.dotin.loan.trade.adapters.driven.persistence.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    @Test
    void customizerPreservesBootTtlAndAppliesStrictVersionedSerialization() {
        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(mock(RedisConnectionFactory.class));
        builder.cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(37))
                .disableCachingNullValues());

        new RedisConfig().redisLoanCacheManagerCustomizer("v9").customize(builder);

        RedisCacheConfiguration customized = builder.cacheDefaults();
        assertThat(customized.getTtlFunction().getTimeToLive("cache", new Object()))
                .isEqualTo(Duration.ofSeconds(37));
        assertThat(customized.getKeyPrefixFor("cache")).startsWith("v9:");
        String encoded = StandardCharsets.UTF_8
                .decode(customized.getValueSerializationPair().write(Map.of("field", "value")))
                .toString();
        assertThat(encoded).contains("field", "value").startsWith("{");
        assertThat(customized.getAllowCacheNullValues()).isFalse();
    }
}
