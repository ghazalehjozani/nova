package ir.dotin.loan.trade.adapters.driven.persistence.config;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import ir.dotin.platform.pangaea.commons.core.cache.CacheKeyspace;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.SslOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

@Slf4j
@Configuration
public class RedisConfig implements CachingConfigurer {

    @Bean(destroyMethod = "shutdown")
    public ClientResources lettuceClientResources(ObservationRegistry observationRegistry) {
        int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
        // Lettuce emits Redis CLIENT spans through Micrometer Observation → the OTel bridge turns them into spans
        // (db.system=redis), restoring the Redis dependency node in APM. The Spring Boot Lettuce observation
        // auto-config backs off because this is a custom ClientResources/ConnectionFactory, so wire it explicitly.
        MicrometerTracing tracing = new MicrometerTracing(observationRegistry, "redis", false);
        return DefaultClientResources.builder()
                .ioThreadPoolSize(threads)
                .computationThreadPoolSize(threads)
                .tracing(tracing)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.sentinel.enabled", havingValue = "true", matchIfMissing = true)
    public LettuceConnectionFactory redisConnectionFactory(
            DataRedisProperties redisProperties,
            ClientResources clientResources,
            @Value("${spring.data.redis.sentinel.master}") String masterName,
            @Value("${spring.data.redis.sentinel.nodes}") String sentinelNodesCsv,
            @Value("${spring.data.redis.ssl.enabled:false}") boolean tlsEnabled) {

        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
        sentinelConfig.setMaster(masterName);
        sentinelConfig.setDatabase(redisProperties.getDatabase());
        if (redisProperties.getPassword() != null) {
            RedisPassword pw = RedisPassword.of(redisProperties.getPassword());
            sentinelConfig.setPassword(pw);
            sentinelConfig.setSentinelPassword(pw);
        }
        for (String node : sentinelNodesCsv.split(",")) {
            String[] hp = node.trim().split(":");
            sentinelConfig.addSentinel(new RedisNode(hp[0], Integer.parseInt(hp[1])));
        }

        Duration commandTimeout =
                redisProperties.getTimeout() != null ? redisProperties.getTimeout() : Duration.ofMillis(3000);

        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofMillis(2000))
                .keepAlive(true)
                .tcpNoDelay(true)
                .build();

        ClientOptions.Builder clientOptionsBuilder = ClientOptions.builder()
                .protocolVersion(ProtocolVersion.RESP3)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .autoReconnect(true)
                .socketOptions(socketOptions)
                .timeoutOptions(TimeoutOptions.enabled(commandTimeout));

        if (tlsEnabled) {
            clientOptionsBuilder.sslOptions(
                    SslOptions.builder().jdkSslProvider().build());
        }

        GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(32);
        poolConfig.setMaxIdle(16);
        poolConfig.setMinIdle(4);
        poolConfig.setMaxWait(Duration.ofMillis(2000));
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder clientCfg =
                LettucePoolingClientConfiguration.builder()
                        .clientResources(clientResources)
                        .commandTimeout(commandTimeout)
                        .shutdownTimeout(Duration.ofMillis(200))
                        .readFrom(ReadFrom.REPLICA_PREFERRED)
                        .clientOptions(clientOptionsBuilder.build())
                        .poolConfig(poolConfig);

        if (tlsEnabled) {
            clientCfg.useSsl();
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(sentinelConfig, clientCfg.build());
        factory.setShareNativeConnection(true);
        factory.setValidateConnection(false);
        return factory;
    }

    @Bean
    public RedisCacheManager redisLoanCacheManager(
            RedisConnectionFactory connectionFactory, @Value("${nova.cache.version}") String cacheKeyVersion) {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        JsonMapper mapper = JsonMapper.builder()
                .activateDefaultTyping(ptv, DefaultTyping.JAVA_LANG_OBJECT, JsonTypeInfo.As.PROPERTY)
                .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .changeDefaultVisibility(vc -> vc.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                        .withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY))
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();

        GenericJacksonJsonRedisSerializer serializer = new GenericJacksonJsonRedisSerializer(mapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .prefixCacheNameWith(CacheKeyspace.versioned("", cacheKeyVersion))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new ResilientCacheErrorHandler();
    }
}
