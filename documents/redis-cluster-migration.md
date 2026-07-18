<div dir="rtl">

# بازنویسی اتصال Redis از Sentinel به Cluster

## هدف و دامنه تغییر

این تغییر کلاس `RedisConfig` در ماژول `adapters/driven/persistence` را با کانفیگ جدید مخزن
`nova-config` هماهنگ می‌کند. توپولوژی قبلی بر پایه Redis Sentinel بود؛ توپولوژی جدید مستقیماً به Redis Cluster
وصل می‌شود و seed nodeها را از `spring.data.redis.cluster.nodes` می‌گیرد.

فایل‌های تغییرکرده در `nova`:

- `adapters/driven/persistence/src/main/java/ir/dotin/loan/trade/adapters/driven/persistence/config/RedisConfig.java`
- `adapters/driven/persistence/src/test/java/ir/dotin/loan/trade/adapters/driven/persistence/config/RedisConfigTest.java`
- `documents/redis-cluster-migration.md`

کانفیگ مبنا در مخزن `nova-config`:

- `kv/core/loan/nova/application/cache.yml`

## جریان runtime

هنگام startup، `bootstrap.yml` کانفیگ context مربوط به Nova را از Consul می‌خواند. Spring Boot مقادیر زیر را
در `DataRedisProperties` bind می‌کند و سپس `RedisConfig` از آن‌ها `RedisClusterConfiguration`،
`ClusterClientOptions` و `LettuceConnectionFactory` را می‌سازد:

<div dir="ltr">

```text
nova-config/cache.yml
        |
        v
Consul KV -> DataRedisProperties -> RedisConfig
                                      |-- RedisClusterConfiguration
                                      |-- ClusterClientOptions
                                      |-- Lettuce pool
                                      v
                              LettuceConnectionFactory
                                      v
                         RedisCacheManager / StringRedisTemplate
```

</div>

ساخت bean اتصال فقط وقتی فعال می‌شود که `spring.data.redis.cluster.nodes` وجود داشته باشد. این شرط باعث می‌شود
پروفایل‌های تستی که connection factory مستقل دارند، مجبور به تعریف Cluster مصنوعی نباشند.

## نگاشت کانفیگ به پیاده‌سازی

| کلید کانفیگ | رفتار در `RedisConfig` |
|---|---|
| `spring.data.redis.cluster.nodes` | ساخت seed nodeهای `RedisClusterConfiguration` |
| `spring.data.redis.cluster.max-redirects` | تنظیم تعداد redirectهای مجاز، در صورت تعریف |
| `spring.data.redis.username` | تنظیم ACL username، در صورت تعریف |
| `spring.data.redis.password` | احراز هویت روی Redis Cluster |
| `spring.data.redis.timeout` | command timeout؛ مقدار fallback برابر ۳ ثانیه است |
| `spring.data.redis.connect-timeout` | socket connect timeout؛ مقدار fallback برابر ۲ ثانیه است |
| `spring.data.redis.ssl.enabled` | فعال‌سازی TLS در Lettuce و client options |
| `spring.data.redis.lettuce.cluster.refresh.period` | فعال‌سازی periodic topology refresh با دوره تعریف‌شده |
| `spring.data.redis.lettuce.cluster.refresh.dynamic-refresh-sources` | تعیین منبع‌های discovery توپولوژی؛ مقدار پیش‌فرض Boot برابر `true` است |
| `spring.data.redis.lettuce.pool.*` | تنظیم `max-active`، `max-idle`، `min-idle` و `max-wait` بدون hard-code |
| `spring.data.redis.lettuce.shutdown-timeout` | timeout خاموش‌شدن Lettuce |

در Spring Boot 4.1 و Lettuce 7.5 همه adaptive topology refresh triggerها به‌صورت پیش‌فرض فعال‌اند؛ به همین دلیل
کد از API منسوخ‌شده `enableAllAdaptiveRefreshTriggers()` استفاده نمی‌کند. کلید `adaptive` موجود در YAML برای
Boot 4.1 زائد است و رفتار runtime این پیاده‌سازی به default جدید Lettuce متکی است.

## تفاوت با پیاده‌سازی Sentinel

- `RedisSentinelConfiguration`، master name و sentinel node parser حذف شدند.
- دیگر password جداگانه‌ای برای Sentinel تنظیم نمی‌شود؛ credential روی خود Cluster قرار می‌گیرد.
- `database` حذف شد، چون Redis Cluster فقط database شماره صفر را پشتیبانی می‌کند.
- `ClientOptions` عمومی با `ClusterClientOptions` جایگزین شد تا redirect و topology refresh واقعاً Cluster-aware
  باشند.
- pool و timeoutها به‌جای مقادیر hard-code شده، از `DataRedisProperties` می‌آیند.

رفتارهای قبلی که عمداً حفظ شدند:

- `RESP3`، reconnect خودکار، keep-alive و رد command هنگام disconnect؛
- `ReadFrom.REPLICA_PREFERRED` برای read؛
- tracing دستی Lettuce با `MicrometerTracing` بدون ثبت command argumentها؛
- `RedisCacheManager` نسخه‌دار، serialization فعلی و TTL یک‌ساعته؛
- `ResilientCacheErrorHandler` و رفتار fail-open برای خطاهای cache.

## تست اضافه‌شده

`RedisConfigTest` بدون نیاز به Redis واقعی این موارد را بررسی می‌کند:

- nodeها، credential و `max-redirects` در `RedisClusterConfiguration`؛
- TLS، timeoutها، `ReadFrom` و مقادیر pool؛
- فعال بودن adaptive refresh پیش‌فرض Lettuce؛
- periodic refresh سی‌ثانیه‌ای و dynamic refresh sourceها؛
- استفاده از `ClusterClientOptions` به‌جای client options عمومی.

فرمان verification متمرکز:

<div dir="ltr">

```bash
/opt/apache-maven-3.9.11/bin/mvn -pl adapters/driven/persistence -am \
  -Dtest=RedisConfigTest -Dsurefire.failIfNoSpecifiedTests=false test
```

</div>

## نکات rollout

- env var الزامی `REDIS_CLUSTER_NODES` باید یک لیست comma-separated از `host:port`های seed باشد.
- `REDIS_PASSWORD` باید credential مربوط به خود Redis Cluster باشد، نه Sentinel.
- در صورت نیاز به TLS، `REDIS_TLS_ENABLED=true` و certificate trust محیط JVM باید آماده باشد.
- این bean تحت `@RefreshScope` نیست؛ تغییر nodeها، credential، TLS، pool یا timeout در Consul connection factory
  موجود را بازسازی نمی‌کند و برای اعمال مطمئن آن‌ها restart/rolling restart سرویس لازم است.
- runbook فعلی `documents/runbooks/RB-0007.redis-sentinel-ha.md` مربوط به توپولوژی قدیمی Sentinel است و برای
  عملیات Cluster باید جداگانه بازنویسی یا با runbook جدید جایگزین شود.

</div>
